package ru.intertrust.cm.core.business.impl.profiling;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 03.02.2017
 *         Time: 19:40
 */
public class StackTrace {
    public static final StackTrace IGNORED = new StackTrace();
    private ThreadInfo threadInfo;
    private List<StackTraceElement> stackTrace;
    private List<StackTraceElement> basePathsStackTrace;
    private String[] basePaths;
    private String[] blackListPaths;
    private boolean isInBlackList;
    private boolean belongsToBasePath;

    private StackTrace() {
    }

    public StackTrace(ThreadInfo threadInfo, List<StackTraceElement> stackTrace, List<StackTraceElement> basePathsStackTrace, String[] basePaths, boolean belongsToBasePath, String[] blackListPaths, boolean isInBlackList) {
        this.threadInfo = threadInfo;
        this.stackTrace = stackTrace;
        this.basePathsStackTrace = basePathsStackTrace;
        this.basePaths = basePaths;
        this.blackListPaths = blackListPaths;
        this.belongsToBasePath = belongsToBasePath;
        this.isInBlackList = isInBlackList;
    }

    public static StackTrace get(Thread thread, StackTraceElement[] stackTrace, String[] basePaths, String[] blackListPaths) {
        boolean checkBlackList = blackListPaths != null && blackListPaths.length > 0;
        ArrayList<StackTraceElement> basePathStackTrace = null;
        boolean isInBlackList = false;
        boolean belongsToBasePath = false;
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (checkBlackList && belongsToOnePathAtLeast(stackTraceElement, blackListPaths)) {
                isInBlackList = true;
            }
            if (belongsToOnePathAtLeast(stackTraceElement, basePaths)) {
                if (basePathStackTrace == null) {
                    basePathStackTrace = new ArrayList<>();
                }
                basePathStackTrace.add(stackTraceElement);
                belongsToBasePath = true;
            }
        }
        return new StackTrace(new ThreadInfo(thread), Arrays.asList(stackTrace), basePathStackTrace, basePaths, belongsToBasePath, blackListPaths, isInBlackList);
    }

    public boolean isInBlackList() {
        return isInBlackList;
    }

    public boolean belongsToBasePath() {
        return belongsToBasePath;
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public List<StackTraceElement> getStackTrace() {
        return stackTrace;
    }

    public List<StackTraceElement> getBasePathsStackTrace() {
        return basePathsStackTrace;
    }

    public boolean approximatelyEquals(StackTrace another) {
        ArrayList<StackTraceElement> thisStackTrace = reverse(basePathsStackTrace);
        ArrayList<StackTraceElement> anotherStackTrace = reverse(another.basePathsStackTrace);
        if (anotherStackTrace.isEmpty()) {
            return false;
        }
        ArrayList<StackTraceElement> arrayToScan = thisStackTrace.size() < anotherStackTrace.size() ? thisStackTrace : anotherStackTrace;
        int nonMatchingIndex = -1;
        for (int i = 0; i < arrayToScan.size(); i++) {
            final StackTraceElement thisElt = thisStackTrace.get(i);
            final StackTraceElement anotherElt = anotherStackTrace.get(i);
            if (!equalStackTraceElements(thisElt, anotherElt)) {
                nonMatchingIndex = i;
                break;
            }
        }
        if (nonMatchingIndex == -1) {
            return true;
        }
        final int minEntriesInStackTrace = Math.min(thisStackTrace.size(), anotherStackTrace.size());
        int matchingEntriesQty = nonMatchingIndex;
        int nonMatchingEntriesQty = minEntriesInStackTrace - nonMatchingIndex; // 2 nonmatching, 2 vs 4 entries, 2 matching
        return (matchingEntriesQty >= minEntriesInStackTrace / 2) || (nonMatchingEntriesQty <= minEntriesInStackTrace / 2 && nonMatchingEntriesQty < 5 || nonMatchingIndex > 7);
    }

    private boolean equalStackTraceElements(StackTraceElement e1, StackTraceElement e2) {
        return e1 == e2 || e1.getClassName().equals(e2.getClassName()) && Objects.equals(e1.getMethodName(), e2.getMethodName());
    }

    private static ArrayList<StackTraceElement> reverse(List<StackTraceElement> stackTrace) {
        final ArrayList<StackTraceElement> result = new ArrayList<>(stackTrace);
        Collections.reverse(result);
        return result;
    }

    public String[] getBasePaths() {
        return basePaths;
    }

    private static boolean belongsToOnePathAtLeast(StackTraceElement stackTraceElement, String[] paths) {
        for (String basePackage : paths) {
            if ((stackTraceElement.getClassName() + '.' + stackTraceElement.getMethodName()).contains(basePackage)) {
                return true;
            }
        }
        return false;
    }

    public String oneLineBasePackageDescription() {
        return threadInfo.id + "->" + basePathsStackTrace.get(0);
    }
}
