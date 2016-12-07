package uk.ac.ebi.quickgo.client.model.presets.impl;

import uk.ac.ebi.quickgo.client.model.presets.CompositePreset;
import uk.ac.ebi.quickgo.client.model.presets.PresetItem;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.mapping;
import static uk.ac.ebi.quickgo.client.model.presets.impl.CompositePresetImpl.PresetType.*;

/**
 * <p>Represents preset information relating to different aspects of QuickGO.
 *
 * <p>Presets returned are ordered by three criteria:
 * <ol>
 *     <li>natural ordering (low to high) by {@link PresetItem#getRelevancy()}</li>
 *     <li>alphabetically by {@link uk.ac.ebi.quickgo.client.model.presets.PresetItem.Property#NAME}}</li>
 *     <li>by insertion order</li>
 * </ol>
 *
 * <p>Presets with the same name will be grouped by {@code name}, and
 * the {@code id}s grouped into {@code associations}.
 *
 * <p>For example, by adding:
 * <ul>
 *     <li>{@link PresetItem} with {@code name} = "n1", {@code id} = "id1", {@code assoc} = "a1"</li>
 *     <li>{@link PresetItem} with {@code name} = "n1", {@code id} = "id2", {@code assoc} = "a2"</li>
 *     <li>{@link PresetItem} with {@code name} = "n2", {@code id} = "id3", {@code assoc} = "a3"</li>
 * </ul>
 *
 * <p>The corresponding grouped presets will be:
 * <ul>
 *     <li>{@link PresetItem} with {@code name} = "n1", {@code associations} = [
 *     <ul>
 *         <li>{@link PresetItem} with {@code name} = "a1", {@code id} = "id1"</li>
 *         <li>{@link PresetItem} with {@code name} = "a2", {@code id} = "id2"</li>
 *     </ul>
 *     ]</li>
 *     <li>{@link PresetItem} with {@code name} = "n3", {@code associations} = [
 *     <ul>
 *         <li>{@link PresetItem} with {@code name} = "a3", {@code id} = "id3"</li>
 *     </ul>
 *     ]</li>
 * </ul>
 *
 * Created 30/08/16
 * @author Edd
 */
public class CompositePresetImpl implements CompositePreset {
    private final EnumMap<PresetType, Set<PresetItem>> presetsMap;

    public CompositePresetImpl() {
        presetsMap = new EnumMap<>(PresetType.class);

        for (PresetType presetType : PresetType.values()) {
            presetsMap.put(presetType, new LinkedHashSet<>());
        }

        initialiseStaticPresets();
    }

    public void addPreset(PresetType presetType, PresetItem presetItem) {
        checkArgument(presetItem != null, "PresetItem cannot be null");

        presetsMap.get(presetType).add(presetItem);
    }

    @Override public List<PresetItem> getAssignedBy() {
        return sortedPresetItems(PresetType.ASSIGNED_BY);
    }

    @Override public List<PresetItem> getReferences() {
        return sortedPresetItems(PresetType.REFERENCES);
    }

    @Override public List<PresetItem> getEvidences() {
        return sortedPresetItems(PresetType.EVIDENCES);
    }

    @Override public List<PresetItem> getWithFrom() {
        return sortedPresetItems(PresetType.WITH_FROM);
    }

    @Override public List<PresetItem> getGeneProducts() {
        return sortedPresetItems(PresetType.GENE_PRODUCT);
    }

    @Override public List<PresetItem> getGoSlimSets() {
        return sortedPresetItems(GO_SLIMS_SETS, goSlimsGrouping());
    }

    @Override public List<PresetItem> getTaxons() {
        return sortedPresetItems(TAXONS);
    }

    @Override public List<PresetItem> getQualifiers() {
        return sortedPresetItems(QUALIFIERS);
    }

    @Override public List<PresetItem> getAspects() {
        return sortedPresetItems(ASPECTS);
    }

    @Override public List<PresetItem> getGeneProductTypes() {
        return sortedPresetItems(GENE_PRODUCT_TYPES);
    }

    private void initialiseStaticPresets() {
        presetsMap.put(ASPECTS, StaticAspects.createAspects());
        presetsMap.put(GENE_PRODUCT_TYPES, StaticGeneProductTypes.createGeneProductTypes());
    }

    /**
     * Sorts the presets according to the ordering rules defined in the class description. This method
     * makes use of the default grouping function defined by {@link CompositePresetImpl#defaultGrouping()}.
     * @param presetType the {@link PresetType} whose list of {@link PresetItem}s are to be to returned.
     * @return the list of {@link PresetItem}s corresponding to the specified {@code presetType}.
     */
    private List<PresetItem> sortedPresetItems(PresetType presetType) {
        return sortedPresetItems(presetType, defaultGrouping());
    }

    /**
     * Sorts the presets according to the ordering rules defined in the class description.
     * @param presetType the {@link PresetType} whose list of {@link PresetItem}s are to be to returned.
     * @param groupingFunction the specific function used to group the {@link PresetItem} instances.
     * @return the list of {@link PresetItem}s corresponding to the specified {@code presetType}.
     */
    private List<PresetItem> sortedPresetItems(
            PresetType presetType,
            Function<Map.Entry<String, List<PresetItem>>, PresetItem> groupingFunction) {
        return presetsMap.get(presetType).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getProperty(PresetItem.Property.NAME),
                        mapping(Function.identity(), Collectors.toList())))
                .entrySet().stream()
                .map(groupingFunction)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Defines the default strategy for grouping a list of {@link PresetItem}s, into a grouped {@link PresetItem}.
     * @return the grouping function.
     */
    private static Function<Map.Entry<String, List<PresetItem>>, PresetItem> defaultGrouping() {
        return groupedEntry -> {
            PresetItem.Builder presetBuilder = PresetItem.createWithName(groupedEntry.getKey());
            groupedEntry.getValue().stream()
                    .findFirst()
                    .map(PresetItem::getProperties)
                    .map(Map::entrySet)
                    .ifPresent(entrySet -> entrySet.forEach(entry ->
                            presetBuilder.withProperty(entry.getKey(), entry.getValue())));

            ifPresetItemMatchesThenApply(groupedEntry.getValue(),
                    p -> p != null && p.getRelevancy() != 0,
                    p -> presetBuilder.withRelevancy(p.getRelevancy()));

            return presetBuilder.build();
        };
    }

    /**
     * Defines the strategy for grouping a list of GO Slim {@link PresetItem}s into a grouped
     * {@link PresetItem}.
     * @return the grouping function.
     */
    private static Function<Map.Entry<String, List<PresetItem>>, PresetItem> goSlimsGrouping() {
        return groupedEntry -> {
            PresetItem.Builder presetBuilder = PresetItem.createWithName(groupedEntry.getKey());
            return presetBuilder.withAssociations(groupedEntry.getValue().stream()
                    .map(CompositePresetImpl::transformGOSlimPreset)
                    .collect(Collectors.toList()))
                    .build();
        };
    }

    private static PresetItem transformGOSlimPreset(PresetItem presetItem) {
        PresetItem.Builder presetItemBuilder = PresetItem
                .createWithName(presetItem.getProperty(PresetItem.Property.NAME))
                .withProperty(PresetItem.Property.ID, presetItem.getProperty(PresetItem.Property.ID));
        if (presetItem.getAssociations() != null) {
            presetItem.getAssociations().stream()
                    .findFirst()
                    .ifPresent(item -> presetItemBuilder.withProperty(
                            SlimAdditionalProperty.NAME.getKey(),
                            item.getProperty(PresetItem.Property.NAME)));
        }

        StaticAspects.Aspect.findByAbbrev(presetItem.getProperty(PresetItem.Property.DESCRIPTION))
                .ifPresent(
                        aspect -> presetItemBuilder.withProperty(SlimAdditionalProperty.ASPECT.getKey(), aspect.name));

        return presetItemBuilder.build();
    }

    /**
     * Given a list of {@link PresetItem}s, if a specified
     * {@link Predicate} is true for a given element of the list, apply some action,
     * defined as a {@link Consumer}.
     * @param presets the list of {@link PresetItem}s
     * @param presetPredicate the {@link Predicate} which must be true for {@code itemConsumer} to be applied
     * @param itemConsumer the {@link Consumer} action to apply to an item
     */
    private static void ifPresetItemMatchesThenApply(
            List<PresetItem> presets,
            Predicate<PresetItem> presetPredicate,
            Consumer<PresetItem> itemConsumer) {

        presets.stream()
                .filter(presetPredicate)
                .findFirst()
                .ifPresent(itemConsumer);
    }

    public enum PresetType {
        ASSIGNED_BY,
        REFERENCES,
        EVIDENCES,
        WITH_FROM,
        GENE_PRODUCT,
        GENE_PRODUCT_TYPES,
        GO_SLIMS_SETS,
        TAXONS,
        QUALIFIERS,
        ASPECTS
    }

    private static class StaticAspects {

        private enum Aspect {
            FUNCTION("Molecular Function", "function", "molecular_function", "F"),
            PROCESS("Biological Process", "process", "biological_process", "P"),
            COMPONENT("Cellular Component", "component", "cellular_component", "C");

            private final String name;
            private final String shortName;
            private final String scientificName;
            private final String abbrev;

            Aspect(String name, String shortName, String scientificName, String abbrev) {
                this.name = name;
                this.shortName = shortName;
                this.scientificName = scientificName;
                this.abbrev = abbrev;
            }

            private static Optional<Aspect> findByAbbrev(String abbrev) {
                for (Aspect aspect : Aspect.values()) {
                    if (aspect.abbrev.equals(abbrev)) {
                        return Optional.of(aspect);
                    }
                }
                return Optional.empty();
            }
        }

        static Set<PresetItem> createAspects() {
            Set<PresetItem> presetAspects = new HashSet<>();
            Arrays.stream(Aspect.values())
                    .forEach(aspect -> insertAspect(presetAspects, aspect));
            return presetAspects;
        }

        private static void insertAspect(Set<PresetItem> presets, Aspect aspect) {
            presets.add(PresetItem
                    .createWithName(aspect.name)
                    .withProperty(PresetItem.Property.ID.getKey(), aspect.scientificName).build());
        }
    }

    private static class StaticGeneProductTypes {

        private enum GeneProductType {
            PROTEINS("Proteins", "protein"),
            RNA("RNA", "miRNA"),
            COMPLEXES("Complexes", "complex");

            private final String name;
            private final String shortName;

            GeneProductType(String name, String shortName) {
                this.name = name;
                this.shortName = shortName;
            }
        }

        static Set<PresetItem> createGeneProductTypes() {
            Set<PresetItem> presetAspects = new HashSet<>();
            Arrays.stream(GeneProductType.values())
                    .forEach(aspect -> insertGeneProductType(presetAspects, aspect));
            return presetAspects;
        }

        private static void insertGeneProductType(Set<PresetItem> presets, GeneProductType geneProductType) {
            presets.add(PresetItem
                    .createWithName(geneProductType.name)
                    .withProperty(PresetItem.Property.ID.getKey(), geneProductType.shortName).build());
        }
    }
}
