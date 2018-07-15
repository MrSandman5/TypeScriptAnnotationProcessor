package com.company;

import javafx.util.Pair;

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
        if (annotatedElements == null || annotatedElements.isEmpty()) {
            return false;
        }
        for (Element annotatedElement : annotatedElements){
            if (annotatedElement.getKind() == ElementKind.CLASS && annotatedElement instanceof TypeElement) {
                TypeElement clazz = (TypeElement)annotatedElement;
                String className = clazz.getSimpleName().toString();

                ArrayList<String> stringNames = new ArrayList<>();
                ArrayList<String> numberNames = new ArrayList<>();
                ArrayList<String> booleanNames = new ArrayList<>();
                fillTypeLists(clazz, stringNames, numberNames, booleanNames);

                Map<String, TypeMirror> methodReturnTypes = new HashMap<>();
                Map<String, List<Pair<String, TypeMirror>>> funcArguments = new HashMap<>();

                for (ExecutableElement method
                        : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
                    TypeMirror methodType = method.getReturnType();
                    String methodName = method.getSimpleName().toString();
                    methodReturnTypes.put(methodName, methodType);
                    funcArguments.put(methodName, new ArrayList<>());
                    for (VariableElement arg : method.getParameters()) {
                        TypeMirror argType = arg.asType();
                        String argName = arg.getSimpleName().toString();
                        funcArguments.get(methodName).add(new Pair<>(argName, argType));
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
                    for (Map.Entry<String, TypeMirror> fun : methodReturnTypes.entrySet()){
                        String funcName = fun.getKey();
                        pw.print("    " + funcName + "(");
                        List<Pair<String, TypeMirror>> argList = funcArguments.get(fun.getKey());
                        if (argList != null) {
                            int size = argList.size();
                            for (Pair<String, TypeMirror> arg : argList) {
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
                                size--;
                                if (size > 0) pw.print(", ");
                            }
                        }
                        if (fun.getValue().toString().equals((String.class.getCanonicalName()))
                                || fun.getValue().getKind().equals(TypeKind.valueOf("CHAR"))) {
                            pw.println(") : string");
                        }
                        else if (fun.getValue().getKind().equals(TypeKind.valueOf("INT"))
                                || fun.getValue().getKind().equals(TypeKind.valueOf("LONG"))
                                || fun.getValue().getKind().equals(TypeKind.valueOf("FLOAT"))
                                || fun.getValue().getKind().equals(TypeKind.valueOf("DOUBLE"))) {
                            pw.println(") : number");
                        }
                        else if (fun.getValue().getKind().equals(TypeKind.valueOf("BOOLEAN"))) {
                            pw.println(") : boolean");
                        }
                        else if (fun.getValue().getKind().equals(TypeKind.valueOf("VOID"))) {
                            pw.println(") : void");
                        }
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

    private void fillTypeLists(TypeElement clazz,
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

}