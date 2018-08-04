package com.company;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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

                Multimap<String, String> primitiveNames = ArrayListMultimap.create();
                findPrimitiveTypes(clazz, primitiveNames);

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
                    for (Map.Entry<String, String> name : primitiveNames.entries()) {
                        pw.println("    " + name.getValue() + " : " + name.getKey());
                    }
                    for (Map.Entry<String, TypeMirror> fun : methodReturnTypes.entrySet()){
                        String funcName = fun.getKey();
                        pw.print("    " + funcName + "(");
                        List<Pair<String, TypeMirror>> argList = funcArguments.get(fun.getKey());
                        if (argList != null) {
                            int size = argList.size();
                            for (Pair<String, TypeMirror> arg : argList) {
                                pw.print(arg.getKey() + ": ");
                                String convertedArg = convertJavaToTypeScriptType(arg.getValue());
                                pw.print(convertedArg);
                                size--;
                                if (size > 0) pw.print(", ");
                            }
                        }
                        String convertedFun = convertJavaToTypeScriptType(fun.getValue());
                        pw.println(") : " + convertedFun);
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

    private String convertJavaToTypeScriptType(TypeMirror clazz) {
        String variableType = clazz.getKind().toString();
        System.out.println(variableType);
        switch (variableType){
            case "CHAR":
                return "string";
            case "BYTE":
                return "number";
            case "SHORT":
                return "number";
            case "INT":
                return "number";
            case "LONG":
                return "number";
            case "FLOAT":
                return "number";
            case "DOUBLE":
                return "number";
            case "BOOLEAN":
                return "boolean";
            case "VOID":
                return "void";
        }
        if (clazz.toString().equals((String.class.getCanonicalName()))) return "string";
        return "";
    }

    private void findPrimitiveTypes(TypeElement clazz, Multimap<String, String> primitiveTypes){
        for (VariableElement field
                : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
            System.out.println(String.class.getSimpleName());
            TypeMirror fieldClass = field.asType();
            String fieldName = field.getSimpleName().toString();
            String convertedType = convertJavaToTypeScriptType(fieldClass);
            primitiveTypes.put(convertedType, fieldName);
        }
    }

    /*private void printInterface(String classname){

    }*/

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