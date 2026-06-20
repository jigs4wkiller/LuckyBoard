package dev.lucky.gboardpatches.extension.symbolfooter;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class GboardSymbolFooterOrderRuntime {
    private static final String TAG = "GboardPatches";
    private static final String LOG_LABEL = "expression footer tab order";

    private static final Map<String, Handles> HANDLES_BY_LOADER =
            Collections.synchronizedMap(new WeakHashMap<String, Handles>());
    private static final Object SETTINGS_LOCK = new Object();
    private static volatile CachedSettings cachedSettings;

    private GboardSymbolFooterOrderRuntime() {}

    public static Object reorderExpressionCorpusList(Object receiver, Object corpusList) {
        Log.i(TAG, "reorderExpressionCorpusList called: receiver=" + receiver.getClass().getName() + ", corpusList=" + corpusList.getClass().getName());
        if (corpusList == null) {
            Log.w(TAG, "corpusList is null, returning");
            return corpusList;
        }
        try {
            ClassLoader classLoader = corpusList.getClass().getClassLoader();
            Log.i(TAG, "classLoader=" + classLoader);
            if (classLoader == null && receiver != null) {
                classLoader = receiver.getClass().getClassLoader();
            }
            if (classLoader == null) {
                Log.w(TAG, "classLoader is null, returning");
                return corpusList;
            }
            Handles handles = handles(classLoader);
            if (handles == null) {
                Log.w(TAG, "handles is null, returning");
                return corpusList;
            }
            Log.i(TAG, "handles created, expressionCorpusItemClass=" + handles.expressionCorpusItemClass);
            List<String> configuredOrder = readPrefsDirectly();
            Log.i(TAG, "configuredOrder=" + configuredOrder);
            if (configuredOrder == null || configuredOrder.isEmpty()) {
                configuredOrder = defaultOrderCopy();
                Log.i(TAG, "using default order");
            }
            return reorderExpressionCorpusList(handles, corpusList, configuredOrder);
        } catch (Throwable throwable) {
            Log.e(TAG, "Failed to reorder " + LOG_LABEL + " corpus list", throwable);
            return corpusList;
        }
    }

    public static void invalidateCachedSettings() {
        synchronized (SETTINGS_LOCK) {
            cachedSettings = null;
        }
    }

    private static Object reorderExpressionCorpusList(Handles handles,
            Object corpusList,
            List<String> configuredOrder) throws Throwable {
        if (!(corpusList instanceof Iterable)) {
            Log.w(TAG, "corpusList is not Iterable: " + corpusList.getClass().getName());
            return corpusList;
        }
        List<OrderEntry> entries = new ArrayList<OrderEntry>();
        int index = 0;
        for (Object corpusItem : (Iterable) corpusList) {
            String keyboardTypeName = null;
            if (corpusItem != null && handles.expressionCorpusItemClass != null
                    && handles.expressionCorpusItemClass.isInstance(corpusItem)
                    && handles.expressionCorpusItemKeyboardTypeField != null) {
                Object keyboardType = handles.expressionCorpusItemKeyboardTypeField.get(corpusItem);
                if (keyboardType != null && handles.keyboardTypeNameField != null) {
                    Object nameObj = handles.keyboardTypeNameField.get(keyboardType);
                    keyboardTypeName = nameObj != null ? String.valueOf(nameObj) : null;
                }
            } else {
                Log.w(TAG, "item type mismatch: item=" + corpusItem.getClass().getName()
                        + " expected=" + (handles.expressionCorpusItemClass != null ? handles.expressionCorpusItemClass.getName() : "null")
                        + " isInstance=" + (handles.expressionCorpusItemClass != null && handles.expressionCorpusItemClass.isInstance(corpusItem))
                        + " keyboardTypeField=" + (handles.expressionCorpusItemKeyboardTypeField != null));
            }
            entries.add(new OrderEntry(corpusItem, keyboardTypeName, index++));
        }
        if (entries.isEmpty()) {
            Log.w(TAG, "entries is empty, returning original list");
            return corpusList;
        }

        StringBuilder sb = new StringBuilder("original order: [");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(entries.get(i).keyboardTypeName);
        }
        sb.append("]");
        Log.i(TAG, sb.toString());

        List<OrderEntry> reorderedEntries = new ArrayList<OrderEntry>(entries);
        Collections.sort(reorderedEntries, new java.util.Comparator<OrderEntry>() {
            @Override
            public int compare(OrderEntry left, OrderEntry right) {
                if (left.keyboardTypeName != null && right.keyboardTypeName != null) {
                    int configuredLeft = configuredOrder.indexOf(left.keyboardTypeName);
                    int configuredRight = configuredOrder.indexOf(right.keyboardTypeName);
                    if (configuredLeft >= 0 && configuredRight >= 0) {
                        return Integer.compare(configuredLeft, configuredRight);
                    }
                    if (configuredLeft >= 0) return -1;
                    if (configuredRight >= 0) return 1;
                    return left.keyboardTypeName.compareTo(right.keyboardTypeName);
                }
                if (left.keyboardTypeName != null) return -1;
                if (right.keyboardTypeName != null) return 1;
                return Integer.compare(left.originalIndex, right.originalIndex);
            }
        });

        StringBuilder sb2 = new StringBuilder("sorted order: [");
        for (int i = 0; i < reorderedEntries.size(); i++) {
            if (i > 0) sb2.append(", ");
            sb2.append(reorderedEntries.get(i).keyboardTypeName);
        }
        sb2.append("]");
        Log.i(TAG, sb2.toString());

        if (isSameOrder(entries, reorderedEntries)) {
            Log.i(TAG, "isSameOrder=true, returning original list unchanged");
            return corpusList;
        }

        Log.i(TAG, "order changed, creating new pzy instance via reflection");
        Object[] sortedArray = new Object[reorderedEntries.size()];
        int idx = 0;
        for (OrderEntry entry : reorderedEntries) {
            sortedArray[idx++] = entry.corpusItem;
        }
        try {
            java.lang.reflect.Constructor<?> ctor =
                    corpusList.getClass().getConstructor(Object[].class, int.class);
            Object result = ctor.newInstance(sortedArray, reorderedEntries.size());
            Log.i(TAG, "SUCCESS: new list type=" + result.getClass().getName()
                    + " size=" + ((java.util.List) result).size());
            return result;
        } catch (Throwable t) {
            Log.e(TAG, "FAILED to create reordered list: " + t.getClass().getSimpleName()
                    + ": " + t.getMessage(), t);
            return corpusList;
        }
    }

    private static boolean isSameOrder(List<OrderEntry> left, List<OrderEntry> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            if (left.get(index).corpusItem != right.get(index).corpusItem) {
                return false;
            }
        }
        return true;
    }

    private static Handles handles(ClassLoader classLoader) {
        String key = classLoader.toString();
        Handles cached = HANDLES_BY_LOADER.get(key);
        if (cached != null) {
            return cached;
        }
        try {
            Handles created = new Handles(classLoader);
            HANDLES_BY_LOADER.put(key, created);
            return created;
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to create Handles for " + key, throwable);
            return null;
        }
    }

    private static final class Handles {
        final Class<?> expressionCorpusItemClass;
        final Field expressionCorpusItemKeyboardTypeField;
        final Field keyboardTypeNameField;

        Handles(ClassLoader classLoader) throws Throwable {
            expressionCorpusItemClass = Class.forName("jgg", false, classLoader);
            expressionCorpusItemKeyboardTypeField = expressionCorpusItemClass.getDeclaredField("c");
            expressionCorpusItemKeyboardTypeField.setAccessible(true);

            Class<?> keyboardTypeClass = Class.forName("kvf", false, classLoader);
            keyboardTypeNameField = keyboardTypeClass.getDeclaredField("m");
            keyboardTypeNameField.setAccessible(true);
        }
    }

    private static class OrderEntry {
        final Object corpusItem;
        final String keyboardTypeName;
        final int originalIndex;

        OrderEntry(Object corpusItem, String keyboardTypeName, int originalIndex) {
            this.corpusItem = corpusItem;
            this.keyboardTypeName = keyboardTypeName;
            this.originalIndex = originalIndex;
        }
    }

    private static class CachedSettings {
        final List<String> order;
        CachedSettings(List<String> order) {
            this.order = order;
        }
    }

    private static List<String> defaultOrderCopy() {
        return new ArrayList<String>(GboardSymbolFooterOrderSettings.DEFAULT_SYMBOL_FOOTER_ORDER);
    }

    private static List<String> readPrefsDirectly() {
        try {
            String[] paths = new String[]{
                "/data/data/pre.lucky.com.google.android.inputmethod.latin/shared_prefs/gboard_symbol_footer_order.xml",
                "/data/data/com.google.android.inputmethod.latin/shared_prefs/gboard_symbol_footer_order.xml"
            };
            for (String path : paths) {
                File file = new File(path);
                if (file.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    String xml = sb.toString();
                    java.util.regex.Matcher m = java.util.regex.Pattern
                            .compile("name=\"pref_symbol_footer_order\">([^<]+)<")
                            .matcher(xml);
                    if (m.find()) {
                        String value = m.group(1);
                        return Arrays.asList(value.split(","));
                    }
                }
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to read prefs directly", throwable);
        }
        return null;
    }
}
