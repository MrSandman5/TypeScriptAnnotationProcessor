package com.company;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import javafx.util.Pair;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
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

                Multimap<String, String> variableNames = ArrayListMultimap.create();
                findVariableTypes(clazz, variableNames);

                Multimap<TypeMirror, String> methodReturnTypes = ArrayListMultimap.create();
                Multimap<String, Pair<TypeMirror, String>> funcArguments = ArrayListMultimap.create();
                findMethodTypes(clazz, methodReturnTypes, funcArguments);

                printInterface(className, variableNames, methodReturnTypes, funcArguments);
            }
        }
        return true;
    }

    private String convertPrimitiveTypes(TypeMirror clazz) {
        String variableType = clazz.getKind().toString();
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
        if (clazz.toString().equals(String.class.getTypeName())) return "string";
        return "";
    }


    private void findVariableTypes(TypeElement clazz, Multimap<String, String> types){
        for (VariableElement field
                : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
            TypeMirror fieldClass = field.asType();
            String fieldName = field.getSimpleName().toString();
            String convertedPrimType = convertPrimitiveTypes(fieldClass);
            types.put(convertedPrimType, fieldName);
        }
    }

    private void findMethodTypes(TypeElement clazz, Multimap<TypeMirror, String> methodtypes, Multimap<String, Pair<TypeMirror, String>> funcarg){
        for (ExecutableElement method
                : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
            TypeMirror methodType = method.getReturnType();
            String methodName = method.getSimpleName().toString();
            methodtypes.put(methodType, methodName);
            for (VariableElement arg : method.getParameters()) {
                TypeMirror argType = arg.asType();
                String argName = arg.getSimpleName().toString();
                funcarg.put(methodName, new Pair<>(argType, argName));
            }
        }
    }

    private void printInterface(String className,
                                Multimap<String, String> variableNames,
                                Multimap<TypeMirror, String> methodReturnTypes,
                                Multimap<String, Pair<TypeMirror, String>> funcArguments){
        try {
            PrintWriter pw = new PrintWriter(className + ".ts", "UTF-8");
            pw.println("interface I" + className + " {");
            for (Map.Entry<String, String> name : variableNames.entries()) {
                pw.println("    " + name.getValue() + " : " + name.getKey());
            }
            for (Map.Entry<TypeMirror, String> fun : methodReturnTypes.entries()){
                String funcName = fun.getValue();
                pw.print("    " + funcName + "(");
                Collection<Pair<TypeMirror, String>> argList = funcArguments.get(fun.getValue());
                if (argList != null) {
                    int size = argList.size();
                    for (Pair<TypeMirror, String> arg : argList) {
                        pw.print(arg.getValue() + ": ");
                        String convertedArg = convertPrimitiveTypes(arg.getKey());
                        pw.print(convertedArg);
                        size--;
                        if (size > 0) pw.print(", ");
                    }
                }
                String convertedFun = convertPrimitiveTypes(fun.getKey());
                pw.println(") : " + convertedFun);
            }
            pw.println("}");
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
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