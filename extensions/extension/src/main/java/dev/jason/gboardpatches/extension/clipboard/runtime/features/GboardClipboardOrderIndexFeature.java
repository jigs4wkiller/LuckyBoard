package dev.jason.gboardpatches.extension.clipboard;

import java.util.List;

final class GboardClipboardOrderIndexFeature {
    private final GboardClipboardRuntimeSupport support;

    GboardClipboardOrderIndexFeature(GboardClipboardRuntimeSupport support) {
        this.support = support;
    }

    int computeClipOrder(GboardClipboardRuntimeSupport.ReflectionHandles handles, List<Object> items,
            Object clip, String orderIndexMode) {
        return support.runSafely("compute clipboard order index", () -> {
            if (items == null || clip == null) {
                return Integer.valueOf(-1);
            }
            boolean oldestFirst =
                    GboardClipboardSettings.CLIPBOARD_ORDER_INDEX_MODE_OLDEST_FIRST.equals(
                            orderIndexMode);
            long targetTimestamp = support.clipTimestamp(handles, clip);
            long targetId = support.clipId(handles, clip);
            int order = 1;
            for (Object candidate : items) {
                if (candidate == null || candidate == handles.recentHeader
                        || candidate == handles.pinnedHeader
                        || candidate == handles.specialHeader) {
                    continue;
                }
                long candidateTimestamp = support.clipTimestamp(handles, candidate);
                long candidateId = support.clipId(handles, candidate);
                if (shouldRankAhead(oldestFirst, candidateTimestamp, candidateId,
                        targetTimestamp, targetId)) {
                    order++;
                }
            }
            return Integer.valueOf(order);
        }, Integer.valueOf(-1)).intValue();
    }

    String formatToken(int clipOrder) {
        return support.runSafely("format clipboard order index token", () -> {
            return clipOrder > 0 ? Integer.toString(clipOrder) : null;
        }, null);
    }

    boolean isToken(String token) {
        return token != null && token.matches("\\d+");
    }

    private boolean shouldRankAhead(boolean oldestFirst, long candidateTimestamp,
            long candidateId, long targetTimestamp, long targetId) {
        if (candidateTimestamp == targetTimestamp) {
            return oldestFirst ? candidateId < targetId : candidateId > targetId;
        }
        return oldestFirst ? candidateTimestamp < targetTimestamp
                : candidateTimestamp > targetTimestamp;
    }
}
