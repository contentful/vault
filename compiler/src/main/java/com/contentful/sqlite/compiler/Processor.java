package com.contentful.sqlite.compiler;

import com.contentful.java.cda.Constants.CDAResourceType;
import com.contentful.sqlite.Asset;
import com.contentful.sqlite.ContentType;
import com.contentful.sqlite.Field;
import com.contentful.sqlite.Resource;
import com.contentful.sqlite.Space;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Type;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.contentful.sqlite.Constants.SUFFIX_SPACE;
import static javax.tools.Diagnostic.Kind.ERROR;

public class Processor extends AbstractProcessor {
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
      TypeElement typeElement = entry.getKey();
      try {
        Injection injection = entry.getValue();
        injection.brewJava().writeTo(filer);
      } catch (Exception e) {
        error(typeElement,
            "Failed writing injection for \"" + typeElement.getQualifiedName() + "\", message: ",
            e.getMessage());
      }
    }
    return true;
  }

  private Map<TypeElement, Injection> findAndParseTargets(RoundEnvironment env) {
    Map<TypeElement, ModelInjector> modelTargets =
        new LinkedHashMap<TypeElement, ModelInjector>();

    Map<TypeElement, SpaceInjection> spaceTargets =
        new LinkedHashMap<TypeElement, SpaceInjection>();

    // Parse ContentType bindings
    for (Element element : env.getElementsAnnotatedWith(ContentType.class)) {
      try {
        parseContentType(element, modelTargets);
      } catch (Exception e) {
        parsingError(element, ContentType.class, e);
      }
    }

    // Parse Space bindings
    for (Element element : env.getElementsAnnotatedWith(Space.class)) {
      try {
        parseSpace(element, spaceTargets, modelTargets);
      } catch (Exception e) {
        parsingError(element, Space.class, e);
      }
    }

    Map<TypeElement, Injection> result = new LinkedHashMap<TypeElement, Injection>();
    result.putAll(spaceTargets);
    return result;
  }

  private void parseSpace(Element element, Map<TypeElement, SpaceInjection> spaceTargets,
      Map<TypeElement, ModelInjector> modelTargets) {

    TypeElement typeElement = (TypeElement) element;
    String id = element.getAnnotation(Space.class).value();
    if (id.isEmpty()) {
      error(element, "@%s id may not be empty. (%s)",
          Space.class.getSimpleName(),
          typeElement.getQualifiedName());
      return;
    }

    if (hasInjection(spaceTargets, id, SpaceInjection.class)) {
      error(element,
          "@%s for \"%s\" cannot be used on multiple classes. (%s)",
          Space.class.getSimpleName(),
          id,
          typeElement.getQualifiedName());
      return;
    }

    TypeMirror spaceMirror = elementUtils.getTypeElement(Space.class.getName()).asType();
    List<ModelInjector> includedModels = new ArrayList<ModelInjector>();
    for (AnnotationMirror mirror : typeElement.getAnnotationMirrors()) {
      if (typeUtils.isSameType(mirror.getAnnotationType(), spaceMirror)) {
        Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> items =
            mirror.getElementValues().entrySet();

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : items) {
          if ("models".equals(entry.getKey().getSimpleName().toString())) {
            List l = (List) entry.getValue().getValue();
            if (l.size() == 0) {
              error(element, "@%s models must not be empty. (%s)",
                  Space.class.getSimpleName(),
                  typeElement.getQualifiedName());
            }

            for (Object model : l) {
              Element e = ((Type) ((Attribute) model).getValue()).asElement();
              //noinspection SuspiciousMethodCalls
              ModelInjector modelInjection = modelTargets.get(e);
              if (modelInjection == null) {
                error(element,
                    "Cannot include model (\"%s\"), is not annotated with @%s. (%s)",
                    e.toString(),
                    ContentType.class.getSimpleName(),
                    typeElement.getQualifiedName());
              } else {
                includedModels.add(modelInjection);
              }
            }
          }
        }
      }
    }

    String targetType = typeElement.getQualifiedName().toString();
    String classPackage = getPackageName(typeElement);
    String className = getClassName(typeElement, classPackage) + SUFFIX_SPACE;
    String tableName = "space_" + SqliteUtils.hashForId(id);

    SpaceInjection injection = new SpaceInjection(id, classPackage, className, targetType,
        includedModels, tableName);

    spaceTargets.put(typeElement, injection);
  }

  private void parseContentType(Element element, Map<TypeElement, ModelInjector> targets) {
    TypeElement typeElement = (TypeElement) element;
    String id = element.getAnnotation(ContentType.class).value();
    if (id.isEmpty()) {
      error(element, "@%s id may not be empty. (%s)",
          ContentType.class.getSimpleName(),
          typeElement.getQualifiedName());
      return;
    }

    if (hasModelInjectorWithId(targets.values(), id)) {
      error(element,
          "@%s for \"%s\" cannot be used on multiple classes. (%s)",
          ContentType.class.getSimpleName(),
          id,
          typeElement.getQualifiedName());
      return;
    }

    if (!isSubtypeOfType(element.asType(), Resource.class.getName())) {
      error(element,
          "Classes annotated with @%s must extend \"" + Resource.class.getName() + "\". (%s)",
          ContentType.class.getSimpleName(),
          typeElement.getQualifiedName());
    }

    Set<ModelMember> members = new LinkedHashSet<ModelMember>();
    Set<String> memberIds = new LinkedHashSet<String>();
    for (Element enclosedElement : element.getEnclosedElements()) {
      Field field = enclosedElement.getAnnotation(Field.class);
      if (field == null) {
        continue;
      }

      String fieldId = field.value();
      if (fieldId.isEmpty()) {
        fieldId = enclosedElement.getSimpleName().toString();
      }

      if (!memberIds.add(fieldId)) {
        error(element,
            "@%s for the same id (\"%s\") was used multiple times in the same class. (%s)",
            Field.class.getSimpleName(),
            fieldId,
            typeElement.getQualifiedName());
        return;
      }

      members.add(createMember(element, typeElement, enclosedElement, fieldId));
    }

    String tableName = "entry_" + SqliteUtils.hashForId(id);

    ModelInjector injection = new ModelInjector(
        id, typeElement.getQualifiedName().toString(), tableName, members);

    targets.put(typeElement, injection);
  }

  private ModelMember createMember(Element element, TypeElement typeElement,
      Element enclosedElement, String fieldId) {
    TypeMirror enclosedType = enclosedElement.asType();
    String className = enclosedType.toString();
    String linkType = getLinkType(enclosedType);
    String fieldName = enclosedElement.getSimpleName().toString();
    String sqliteType = null;

    if (linkType == null) {
      sqliteType = SqliteUtils.typeForClass(className);
      if (sqliteType == null) {
        error(element,
            "@%s specified for unsupported type (\"%s\"). (%s.%s)",
            Field.class.getSimpleName(),
            className,
            typeElement.getQualifiedName(),
            enclosedElement.getSimpleName());
      }
    }

    return ModelMember.builder()
        .setId(fieldId)
        .setFieldName(fieldName)
        .setClassName(className)
        .setSqliteType(sqliteType)
        .setLinkType(linkType)
        .build();
  }

  private String getLinkType(TypeMirror typeMirror) {
    CDAResourceType resourceType = null;

    if (isSubtypeOfType(typeMirror, Resource.class.getName())) {
      if (isSubtypeOfType(typeMirror, Asset.class.getName())) {
        resourceType = CDAResourceType.Asset;
      } else {
        resourceType = CDAResourceType.Entry;
      }
    }

    if (resourceType == null) {
      return null;
    }

    return resourceType.toString();
  }

  private boolean hasInjection(Map<TypeElement, ? extends Injection> targets,
      String id, Class<? extends Injection> injectionClass) {
    for (Injection target : targets.values()) {
      if (id.equals(target.id) && injectionClass.isInstance(target)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasModelInjectorWithId(Collection<ModelInjector> targets, String id) {
    for (ModelInjector target : targets) {
      if (id.equals(target.id)) {
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

  private boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
    if (otherType.equals(typeMirror.toString())) {
      return true;
    }
    if (!(typeMirror instanceof DeclaredType)) {
      return false;
    }
    DeclaredType declaredType = (DeclaredType) typeMirror;
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    if (typeArguments.size() > 0) {
      StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
      typeString.append('<');
      for (int i = 0; i < typeArguments.size(); i++) {
        if (i > 0) {
          typeString.append(',');
        }
        typeString.append('?');
      }
      typeString.append('>');
      if (typeString.toString().equals(otherType)) {
        return true;
      }
    }
    Element element = declaredType.asElement();
    if (!(element instanceof TypeElement)) {
      return false;
    }
    TypeElement typeElement = (TypeElement) element;
    TypeMirror superType = typeElement.getSuperclass();
    if (isSubtypeOfType(superType, otherType)) {
      return true;
    }
    for (TypeMirror interfaceType : typeElement.getInterfaces()) {
      if (isSubtypeOfType(interfaceType, otherType)) {
        return true;
      }
    }
    return false;
  }
}
