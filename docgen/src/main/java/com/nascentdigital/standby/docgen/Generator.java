package com.nascentdigital.standby.docgen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.nascentdigital.pipeline.Grouping;
import com.nascentdigital.pipeline.Pipeline;
import com.nascentdigital.standby.Promise;
import com.nascentdigital.standby.annotations.Group;
import com.nascentdigital.standby.annotations.GroupType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;

public class Generator {

    public static String readSample(String filePath){
        String line;
        String str = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader("docgen/samples/" + filePath));

            // getting rid of the first line
            reader.readLine();

            // start reading
            line = reader.readLine();
            while (line != null) {
                str += line;
                line = reader.readLine();
                str += "\n";
            }
        } catch (IOException ex) {
            System.err.println("File with file path: " + filePath + " is missing.");
        }
        return str;
    }

    public static void main(String[] args) throws IOException {


        // fail if file doesn't exist
        File standbyFile = args.length == 0
                ? null
                : new File(args[0]);
        if (standbyFile == null
                || !standbyFile.exists()) {
            System.err.println("Invalid file specified: " + standbyFile);
            System.exit(1);
            return;
        }

        // parse file
        CompilationUnit compilationUnit = JavaParser.parse(standbyFile);

        // fail if standby isn't loaded
        Optional<ClassOrInterfaceDeclaration> standbyClassRef =
                compilationUnit.getClassByName("Promise");
        if (!standbyClassRef.isPresent()) {
            System.err.println("Unable to load Standby from file:" + standbyFile);
            System.exit(1);
            return;
        }

        // fail if standby can't be resolved
        ClassOrInterfaceDeclaration standbyClass = standbyClassRef.get();
        if (standbyClass == null) {
            System.err.println("Unable to resolve Promise class from file: " + standbyFile);
            System.exit(1);
            return;
        }

        // create mapping of method to methodDeclaration
        Map<String, MethodDeclaration> methodMetadataMap = Pipeline.from(standbyClass.getMethods())
                .toMap(md -> {
                    String signature = Pipeline.from(md.getParameters())
                            .map(p -> p.getType().toString())  //using just the parameter types
                            .join(",");
                    return md.getName().toString() + ":" + signature;
                });

        // process all groups
        Pipeline<Grouping<Group, Method>> methodGroups = Pipeline.from(Promise.class.getDeclaredMethods())
//                .where(m -> Modifier.isPublic(m.getModifiers()))
                .groupBy(m -> m.getAnnotation(Group.class));

        JSONObject obj = new JSONObject();

        // process through all methods in each group and get all required information
        for (Grouping<Group, Method> methodGroup : methodGroups) {

            JSONArray methodArray = new JSONArray();
            GroupType annoType = methodGroup.iterator().next().getAnnotation(Group.class).type();

            // add all methods to the group
            JSONObject methodObj;

            for (Method method : methodGroup) {
                methodObj = new JSONObject();

                // get associated metadata for method
                String methodKey = method.getName() + ":"  + Pipeline.from(method.getParameters())
                        .map(p -> {
                            String str = p.getParameterizedType()
                                    .getTypeName()
                                    .replace("java.lang.","")
                                    .replace("com.nascentdigital.standby.","")
                                    .replace("java.util.","");
                            return str;
                        })
                        .join(",");

                // get the actual method declaration related to the method
                MethodDeclaration methodMeta = methodMetadataMap.get(methodKey);

                // get methodKey without punctuation
                String key = Pipeline.from(methodKey.split(",| |<|>|\\[\\]|:|\\?"))
                        .join("");

                methodObj.put("MethodKey", key);

                String signature = methodMeta.getName() + " ("
                        + Pipeline.from(methodMeta.getParameters())
                        .map(p -> p.getType().toString())
                        .join(", ") + ")";

                methodObj.put("MethodSignature", signature);
                methodObj.put("MethodName", methodMeta.getName());
                methodObj.put("ReturnType", methodMeta.getType());

                String name = method.getName() + "(" + Pipeline.from(method.getParameterTypes())
                        .map(p -> {
                            String str = p.getTypeName()
                                    .replace("java.lang.","")
                                    .replace("com.nascentdigital.standby.","")
                                    .replace("java.util.","");
                            return str;
                        }).join(",") + ")";
                methodObj.put("Key", name);

                // get method comment, if one format does not work, try another format
                String comment = methodMeta.getJavadoc().get().getDescription().toText();
                methodObj.put("Comment", comment);

                // get all information about each parameter
                JSONArray paramArray = new JSONArray();
                // create mapping of param comment to parameter
                Map<String, String> paramCommentMap = Pipeline.from(methodMeta.getJavadoc().get().getBlockTags())
                        .where(p -> p.getType().toString() == "PARAM")
                        .toMap(p -> p.getName().get(), p -> p.getContent().toText());

                for (Parameter param : methodMeta.getParameters()) {
                    JSONObject paramObj = new JSONObject();
                    paramObj.put("Type", param.getType());
                    paramObj.put("Name", param.getName());

                    String paramDescrip = paramCommentMap.get(param.getNameAsString());
                    paramObj.put("ParamDescrip", paramDescrip);
                    paramArray.put(paramObj);
                }

                String exception = Pipeline.from(methodMeta.getJavadoc().get().getBlockTags())
                        .where(p -> p.getType().toString() == "THROWS")
                        .map(t -> t.getContent().toText())
                        .join("");

                methodObj.put("Throws", exception);

                // get the code sample
//                String sample = readSample(annoType.toString() + "/" + methodKey + ".md");
//                methodObj.put("Example", sample);

                methodObj.put("Parameters", (Object) paramArray);
                methodArray.put(methodObj);
                obj.put(annoType.name, methodArray);


            }
        }

        System.out.println(obj.toString(2));
    }

}
