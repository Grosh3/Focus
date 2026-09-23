package com.mikesuvade.focus.data.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RepositoryUtils {

    private RepositoryUtils() {}

    private static final java.util.Set<String> STOP_WORDS = new java.util.HashSet<>(java.util.Arrays.asList(
            "в", "во", "на", "и", "с", "со", "к", "ко", "от", "по", "за", "из",
            "до", "у", "о", "об", "для", "при", "над", "под", "без", "через",
            "не", "а", "но", "или", "№"
    ));

    static String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    static boolean containsLatin(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return true;
            }
        }
        return false;
    }

    static boolean isMarkingQuery(String cleanQuery) {
        if (cleanQuery == null) return false;
        if (cleanQuery.length() < 3) return false;
        return cleanQuery.matches("^[А-Яа-яЁё]+[0-9].*");
    }

    static String buildKksQuery(String cleanQuery) {
        Matcher m = Pattern.compile("^[A-Za-z][0-9]+").matcher(cleanQuery);
        if (m.find()) {
            String rest = cleanQuery.substring(m.end());
            if (rest.length() >= 3) {
                return rest;
            }
            return cleanQuery;
        }

        String rest = cleanQuery.replaceFirst("^[0-9]+", "");
        if (!rest.isEmpty() && rest.length() >= 3) {
            return rest;
        }
        return cleanQuery;
    }

    static List<String> buildWordPrefixes(String query) {
        List<String> prefixes = new ArrayList<>();
        if (query == null) return prefixes;

        String lower = query.toLowerCase().trim();
        if (lower.isEmpty()) return prefixes;

        String[] words = lower.split("\\s+");
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (STOP_WORDS.contains(word)) continue;
            if (word.length() <= 2) continue;

            String prefix = word;
            if (word.length() >= 7) {
                prefix = word.substring(0, word.length() - 2);
            } else if (word.length() >= 5) {
                prefix = word.substring(0, word.length() - 1);
            }

                prefixes.add(prefix);

        }
        return prefixes;
    }

    static String buildFieldCondition(String field, int prefixCount) {
        if (prefixCount <= 0) return "";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < prefixCount; i++) {
            if (i > 0) sb.append(" AND ");
            sb.append("LOWER(").append(field).append(") LIKE LOWER(?)");
        }
        sb.append(")");
        return sb.toString();
    }

    static int naturalCompare(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        int i = 0, j = 0;
        int lenA = a.length(), lenB = b.length();

        while (i < lenA && j < lenB) {
            char ca = a.charAt(i);
            char cb = b.charAt(j);

            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                int startI = i;
                int startJ = j;
                while (i < lenA && Character.isDigit(a.charAt(i))) i++;
                while (j < lenB && Character.isDigit(b.charAt(j))) j++;

                String numA = a.substring(startI, i);
                String numB = b.substring(startJ, j);

                java.math.BigInteger bigA = new java.math.BigInteger(numA);
                java.math.BigInteger bigB = new java.math.BigInteger(numB);
                int cmp = bigA.compareTo(bigB);
                if (cmp != 0) return cmp;
            } else {
                char lowerA = Character.toLowerCase(ca);
                char lowerB = Character.toLowerCase(cb);
                if (lowerA != lowerB) return Character.compare(lowerA, lowerB);
                i++;
                j++;
            }
        }

        return Integer.compare(lenA - i, lenB - j);
    }

    static int getGateValveRelevanceScore(com.mikesuvade.focus.domain.models.GateValve valve, String cleanQuery) {
        String isy = valve.getIsy() != null
                ? valve.getIsy().toLowerCase().replaceAll("[\\s\\-\\.\\u2013\\u2014]", "")
                : "";
        String name = valve.getName() != null
                ? valve.getName().toLowerCase().replaceAll("[\\s\\-\\.\\u2013\\u2014]", "")
                : "";
        String fullName = valve.getFullName() != null ? valve.getFullName().toLowerCase() : "";
        String powerCabinet = valve.getPowerCabinet() != null ? valve.getPowerCabinet().toLowerCase() : "";

        if (isy.equals(cleanQuery)) return 1;
        if (name.equals(cleanQuery)) return 2;

        if (isy.startsWith(cleanQuery)) return 10;
        if (name.startsWith(cleanQuery)) return 11;

        if (isy.contains(cleanQuery)) return 20;
        if (name.contains(cleanQuery)) return 21;

        if (fullName.contains(cleanQuery)) return 30;
        if (powerCabinet.contains(cleanQuery)) return 31;

        return 100;
    }

    static int getSensorRelevanceScore(com.mikesuvade.focus.domain.models.Sensor sensor, String query, String kksQuery) {
        String stMarking = sensor.getStMarkir() != null ? sensor.getStMarkir().toLowerCase() : "";
        String fullName = sensor.getFullName() != null ? sensor.getFullName().toLowerCase() : "";
        String name = sensor.getName() != null ? sensor.getName().toLowerCase() : "";
        String kks = sensor.getKks() != null ? sensor.getKks().toLowerCase() : "";

        if (stMarking.equals(query)) return 1;
        if (fullName.equals(query)) return 2;
        if (name.equals(query)) return 3;
        if (kks.equals(kksQuery)) return 4;
        if (kks.equals(query)) return 5;

        if (stMarking.contains(query)) return 10;
        if (fullName.contains(query)) return 20;
        if (name.contains(query)) return 30;
        if (kks.contains(kksQuery)) return 40;
        if (kks.contains(query)) return 50;

        if (stMarking.startsWith(query)) return 11;
        if (fullName.startsWith(query)) return 21;
        if (kks.startsWith(kksQuery)) return 41;

        return 100;
    }

    static int getSetpointRelevanceScore(com.mikesuvade.focus.domain.models.Setpoint setpoint, String query) {
        String name = setpoint.getName() != null ? setpoint.getName().toLowerCase() : "";
        String positionName = setpoint.getPositionName() != null ? setpoint.getPositionName().toLowerCase() : "";
        String setpointValue = setpoint.getSetpointValue() != null ? setpoint.getSetpointValue().toLowerCase() : "";
        String operation = setpoint.getOperation() != null ? setpoint.getOperation().toLowerCase() : "";
        String notes = setpoint.getNotes() != null ? setpoint.getNotes().toLowerCase() : "";

        if (name.equals(query)) return 1;
        if (positionName.equals(query)) return 2;
        if (setpointValue.equals(query)) return 3;
        if (operation.equals(query)) return 4;
        if (notes.equals(query)) return 5;

        if (name.contains(query)) return 10;
        if (positionName.contains(query)) return 20;
        if (setpointValue.contains(query)) return 30;
        if (operation.contains(query)) return 40;
        if (notes.contains(query)) return 50;

        if (name.startsWith(query)) return 11;
        if (positionName.startsWith(query)) return 21;

        return 100;
    }
}