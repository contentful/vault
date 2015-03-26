package com.contentful.sqliteprocessor;

import com.contentful.sqliteprocessor.ContentTypeInjection.Member;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import static javax.tools.Diagnostic.Kind.ERROR;

public class SqliteProcessor extends AbstractProcessor {
  public static final String SUFFIX_MODEL = "$$Model";
  public static final String SUFFIX_SPACE = "$$Space";

  private Elements elementUtils;
  private Types typeUtils;
  private Filer filer;

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<String>();
    types.add(ContentType.class.getCanonicalName());
    types.add(Space.class.getCanonicalName());
    return types;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    filer = processingEnv.getFiler();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Map<TypeElement, Injection> targets = findAndParseTargets(roundEnv);
    for (Map.Entry<TypeElement, Injection> entry : targets.entrySet()) {
      try {
        TypeElement typeElement = entry.getKey();
        Injection injection = entry.getValue();
        JavaFileObject jfo = filer.createSourceFile(injection.getFqcn(), typeElement);
        Writer writer = jfo.openWriter();
        writer.write(injection.brewJava());
        writer.flush();
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return true;
  }

  private Map<TypeElement, Injection> findAndParseTargets(RoundEnvironment env) {
    Map<TypeElement, Injection> targets =
        new LinkedHashMap<TypeElement, Injection>();

    // Parse ContentType injections
    for (Element element : env.getElementsAnnotatedWith(ContentType.class)) {
      try {
        parseContentType(element, targets);
      } catch (Exception e) {
        parsingError(element, ContentType.class, e);
      }
    }

    // Parse Space injections
    for (Element element : env.getElementsAnnotatedWith(Space.class)) {
      try {
        parseSpace(element, targets);
      } catch (Exception e) {
        parsingError(element, Space.class, e);
      }
    }
    return targets;
  }

  private void parseSpace(Element element, Map<TypeElement, Injection> targets) {
    TypeElement typeElement = (TypeElement) element;
    String id = element.getAnnotation(Space.class).value();
    if (id.isEmpty()) {
      error(element, "@%s id may not be empty. (%s)",
          Space.class.getSimpleName(),
          typeElement.getQualifiedName());
      return;
    }
  }

  private void parseContentType(Element element, Map<TypeElement, Injection> targets) {
    TypeElement typeElement = (TypeElement) element;
    String id = element.getAnnotation(ContentType.class).value();
    if (id.isEmpty()) {
      error(element, "@%s id may not be empty. (%s)",
          ContentType.class.getSimpleName(),
          typeElement.getQualifiedName());
      return;
    }

    if (hasContentTypeInjectionWithId(targets, id)) {
      error(element, "@%s for \"%s\" cannot be used on multiple classes. (%s)",
          ContentTypeInjection.class.getSimpleName(),
          id,
          typeElement.getQualifiedName());
      return;
    }

    Set<Member> members = new LinkedHashSet<Member>();
    Set<String> memberIds = new LinkedHashSet<String>();
    for (Element enclosedElement : element.getEnclosedElements()) {
      Field field = enclosedElement.getAnnotation(Field.class);
      if (field == null) {
        continue;
      }

      String fieldId = field.value();
      if (fieldId.isEmpty()) {
        error(enclosedElement, "@%s id may not be empty. (%s.%s)", Field.class.getSimpleName(),
            typeElement.getQualifiedName(),
            enclosedElement.getSimpleName());
        return;
      }

      if (!memberIds.add(fieldId)) {
        error(element,
            "@%s for the same id (\"%s\") was used multiple times in the same class. (%s)",
            Field.class.getSimpleName(),
            fieldId,
            typeElement.getQualifiedName());
        return;
      }

      String className = enclosedElement.asType().toString();
      if (SqliteUtils.typeForClass(className) == null) {
        error(element,
            "@%s specified for unsupported type (\"%s\"). (%s.%s)",
            Field.class.getSimpleName(),
            className,
            typeElement.getQualifiedName(),
            enclosedElement.getSimpleName());
      }

      String fieldName = enclosedElement.getSimpleName().toString();
      members.add(new Member(fieldId, fieldName, className));
    }

    String targetType = typeElement.getQualifiedName().toString();
    String classPackage = getPackageName(typeElement);
    String className = getClassName(typeElement, classPackage) + SUFFIX_MODEL;

    ContentTypeInjection injection = new ContentTypeInjection(
        id, classPackage, className, targetType, members);

    targets.put(typeElement, injection);
  }

  private boolean hasContentTypeInjectionWithId(Map<TypeElement, ? extends Injection> targets,
      String id) {
    for (Injection target : targets.values()) {
      if (id.equals(target.id) && target instanceof ContentTypeInjection) {
        return true;
      }
    }
    return false;
  }

  private String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }

  private static String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }

  private void parsingError(Element element, Class<? extends Annotation> annotation, Exception e) {
    StringWriter stackTrace = new StringWriter();
    e.printStackTrace(new PrintWriter(stackTrace));
    error(element, "Unable to parse @%s injection.\n\n%s", annotation.getSimpleName(), stackTrace);
  }

  private void error(Element element, String message, Object... args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnv.getMessager().printMessage(ERROR, message, element);
  }
}
