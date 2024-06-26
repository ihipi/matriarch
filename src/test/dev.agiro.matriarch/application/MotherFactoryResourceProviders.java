package dev.agiro.matriarch.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.agiro.matriarch.model.ClassProperties;
import dev.agiro.matriarch.model.WrongFormatException;
import dev.agiro.matriarch.model.annotations.ObjectMotherResource;
import dev.agiro.matriarch.model.annotations.OverrideField;
import dev.agiro.matriarch.model.Overrider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MotherFactoryResourceProviders implements ArgumentsProvider, AnnotationConsumer<ObjectMotherResource> {

    private Object[] args;

    private final ObjectMotherGenerator generator = new ObjectMotherGenerator();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @java.lang.Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return Arrays.stream(new Arguments[]{Arguments.arguments(args)});
    }

    @java.lang.Override
    public void accept(ObjectMotherResource objectMotherResource) {
        this.args = Stream.of(objectMotherResource.args())
                .map(argsResource -> generator.create(new ClassProperties(argsResource.targetClass(),
                                                  computeOverrideDefinitions(argsResource.overrides(),
                                                                             argsResource.jsonOverrides()),
                                                                      "")))
                .toArray();
    }

    private Map<String, Overrider> computeOverrideDefinitions(OverrideField[] overrides,
                                                              String jsonOverrides) {
        final Map<String, Overrider> overrideValues = Arrays.stream(overrides)
                .collect(Collectors.toMap(
                        OverrideField::field,
                        overrideValue -> new Overrider(overrideValue.value(),
                                                      overrideValue.isRegexPattern())));

        try {
            flattenNodes(objectMapper.readTree(jsonOverrides), "", overrideValues);
        } catch (JsonProcessingException e) {
            throw new WrongFormatException("Error in Json format : " + jsonOverrides);
        }
        return overrideValues;
    }

    private void flattenNodes(JsonNode node, String currentPath, Map<String, Overrider> flattenedMap) {
        if (node.isValueNode()) {
            flattenedMap.put(currentPath, new Overrider(node.asText(), false));
        } else if (node.isObject()) {

            node.fields().forEachRemaining(entry -> {
                String newPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey();
                flattenNodes(entry.getValue(), newPath, flattenedMap);
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newPath = currentPath + ".[" + i + "]" ;
                flattenNodes(node.get(i), newPath, flattenedMap);
            }
        }
    }


}
