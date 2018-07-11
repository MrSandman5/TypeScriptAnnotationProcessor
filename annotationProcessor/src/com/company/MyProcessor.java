package com.company;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class MyProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment env){
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<? extends Element> annotatedElements =
                env.getElementsAnnotatedWith(TypeScriptAnnotation.class);
        if (annotatedElements.size() == 0) {
            return true;
        }
        for (Element annotatedElement : annotatedElements){
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                ArrayList<String> stringNames = new ArrayList<>();
                ArrayList<String> numberNames = new ArrayList<>();
                ArrayList<String> booleanNames = new ArrayList<>();
                Map<String, TypeMirror> function = new HashMap<>();
                Map<String, Map<String, TypeMirror>> funcArguments = new HashMap<>();
                TypeElement clazz = (TypeElement)annotatedElement;
                String className = clazz.getSimpleName().toString();
                fillTypeLists(clazz, stringNames, numberNames, booleanNames);
                for (ExecutableElement field
                        : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
                    TypeMirror fieldType = field.getReturnType();
                    String fieldName = field.getSimpleName().toString();
                    function.put(fieldName, fieldType);
                    for (VariableElement arg : field.getParameters()) {
                        Map<String, TypeMirror> temp = new HashMap<>();
                        TypeMirror argType = arg.asType();
                        String argName = arg.getSimpleName().toString();
                        temp.put(argName, argType);
                        funcArguments.put(fieldName, temp);
                    }
                }
                try {
                    PrintWriter pw = new PrintWriter(className + ".ts", "UTF-8");
                    pw.println("interface I" + className + " {");
                    for (String stringName : stringNames) {
                        pw.println("    " + stringName + " : string");
                    }
                    for (String numberName : numberNames) {
                        pw.println("    " + numberName + " : number");
                    }
                    for (String booleanName : booleanNames) {
                        pw.println("    " + booleanName + " : boolean");
                    }
                    for (Map.Entry<String, TypeMirror> fun : function.entrySet()){
                        pw.print("    " + fun.getKey() + "(");
                        Map<String, TypeMirror> temp = funcArguments.get(fun.getKey());
                        if (temp != null) {
                            for (Map.Entry<String, TypeMirror> arg : temp.entrySet()) {
                                pw.print(arg.getKey() + ": ");
                                if (arg.getValue().toString().equals((String.class.getCanonicalName()))
                                        || arg.getValue().getKind().equals(TypeKind.valueOf("CHAR"))) {
                                    pw.print("string");
                                } else if (arg.getValue().getKind().equals(TypeKind.valueOf("INT"))
                                        || arg.getValue().getKind().equals(TypeKind.valueOf("LONG"))
                                        || arg.getValue().getKind().equals(TypeKind.valueOf("FLOAT"))
                                        || arg.getValue().getKind().equals(TypeKind.valueOf("DOUBLE"))) {
                                    pw.print("number");
                                } else if (arg.getValue().getKind().equals(TypeKind.valueOf("BOOLEAN"))) {
                                    pw.print("boolean");
                                }
                                pw.print(", ");
                            }
                        }
                        pw.println(") : " + fun.getValue().getKind().toString().toLowerCase());
                    }
                    pw.println("}");
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(TypeScriptAnnotation.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public void fillTypeLists(TypeElement clazz,
                              ArrayList<String> stringNames,
                              ArrayList<String> numberNames,
                              ArrayList<String> booleanNames){
        for (VariableElement field
                : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
            TypeMirror fieldClass = field.asType();
            String fieldName = field.getSimpleName().toString();
            if (fieldClass.toString().equals((String.class.getCanonicalName()))
                    || fieldClass.getKind().equals(TypeKind.valueOf("CHAR"))) {
                stringNames.add(fieldName);
            } else if (fieldClass.getKind().equals(TypeKind.valueOf("INT"))
                    || fieldClass.getKind().equals(TypeKind.valueOf("LONG"))
                    || fieldClass.getKind().equals(TypeKind.valueOf("FLOAT"))
                    || fieldClass.getKind().equals(TypeKind.valueOf("DOUBLE"))) {
                numberNames.add(fieldName);
            } else if (fieldClass.getKind().equals(TypeKind.valueOf("BOOLEAN"))) {
                booleanNames.add(fieldName);
            }
        }
    }
}