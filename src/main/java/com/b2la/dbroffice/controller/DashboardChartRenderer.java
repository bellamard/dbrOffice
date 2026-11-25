package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.dao.Accounts;
import com.b2la.dbroffice.dao.Operation;
import com.b2la.dbroffice.dao.Role;
import com.b2la.dbroffice.preference.StateOper;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Draws the dashboard's compact operational charts without introducing another chart dependency. */
final class DashboardChartRenderer {
    private static final Color INK = Color.web("#17324d");
    private static final Color MUTED = Color.web("#718096");
    private static final Color GRID = Color.web("#e7edf3");
    private static final Color TEAL = Color.web("#0b8f87");
    private static final Color BLUE = Color.web("#4385d1");
    private static final Color AMBER = Color.web("#e6a23c");
    private static final Color RED = Color.web("#d96c67");
    private static final Color PURPLE = Color.web("#8874c8");
    private static final Color[] PALETTE = {TEAL, BLUE, AMBER, RED, PURPLE, Color.web("#4da3a8")};
    private static final List<String> ROLES = List.of("CLIENT", "AGENT", "OFFICE", "ADMIN", "ENTITES", "SUPERVISOR");

    private DashboardChartRenderer() {}

    static void renderAll(Canvas pareto, Canvas line, Canvas sagittal, Canvas mosaic, List<Operation> operations) {
        List<Operation> safeOperations = operations == null ? List.of() : operations;
        renderPareto(pareto, safeOperations);
        renderDailyLine(line, safeOperations);
        renderSagittal(sagittal, safeOperations);
        renderMosaic(mosaic, safeOperations);
    }

    private static void renderPareto(Canvas canvas, List<Operation> operations) {
        GraphicsContext gc = prepare(canvas);
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("Validées", 0);
        counts.put("En attente", 0);
        counts.put("Annulées", 0);
        counts.put("Bloquées", 0);
        counts.put("Autres", 0);
        for (Operation operation : operations) {
            StateOper state = operation == null || operation.getState() == null ? null : operation.getState().getLibelle();
            String label = state == StateOper.VALIDER ? "Validées"
                    : state == StateOper.ATTENTE ? "En attente"
                    : state == StateOper.ANNULER ? "Annulées"
                    : state == StateOper.BLOQUER ? "Bloquées" : "Autres";
            counts.compute(label, (key, value) -> value + 1);
        }
        List<Map.Entry<String, Integer>> entries = counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();
        if (entries.isEmpty()) {
            empty(gc, canvas, "Aucune opération à analyser");
            return;
        }

        double left = 42, top = 22, plotWidth = canvas.getWidth() - 86, plotHeight = canvas.getHeight() - 76;
        int max = entries.stream().mapToInt(Map.Entry::getValue).max().orElse(1);
        double barWidth = Math.min(42, (plotWidth / entries.size()) * .52);
        gc.setFont(Font.font("Segoe UI", 9));
        gc.setTextAlign(TextAlignment.RIGHT);
        for (int step = 0; step <= 4; step++) {
            double y = top + plotHeight - plotHeight * step / 4.0;
            gc.setStroke(GRID);
            gc.setLineWidth(1);
            gc.strokeLine(left, y, left + plotWidth, y);
            gc.setFill(MUTED);
            gc.fillText(String.valueOf(Math.round(max * step / 4.0)), left - 7, y + 3);
        }
        double total = entries.stream().mapToInt(Map.Entry::getValue).sum();
        double running = 0;
        List<double[]> points = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<String, Integer> entry = entries.get(index);
            double centerX = left + plotWidth * (index + .5) / entries.size();
            double barHeight = plotHeight * entry.getValue() / max;
            gc.setFill(PALETTE[index % PALETTE.length]);
            gc.fillRoundRect(centerX - barWidth / 2, top + plotHeight - barHeight, barWidth, barHeight, 7, 7);
            gc.setFill(INK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(entry.getValue()), centerX, top + plotHeight - barHeight - 5);
            gc.setFill(MUTED);
            gc.fillText(entry.getKey(), centerX, top + plotHeight + 17);
            running += entry.getValue();
            points.add(new double[]{centerX, top + plotHeight - plotHeight * running / total});
        }
        gc.setStroke(TEAL);
        gc.setLineWidth(2.5);
        for (int i = 1; i < points.size(); i++) {
            gc.strokeLine(points.get(i - 1)[0], points.get(i - 1)[1], points.get(i)[0], points.get(i)[1]);
        }
        gc.setFill(TEAL);
        points.forEach(point -> gc.fillOval(point[0] - 3.5, point[1] - 3.5, 7, 7));
        double thresholdY = top + plotHeight * .2;
        gc.setStroke(AMBER);
        gc.setLineDashes(4, 3);
        gc.strokeLine(left, thresholdY, left + plotWidth, thresholdY);
        gc.setLineDashes(null);
        gc.setFill(AMBER.darker());
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("80 %", left + plotWidth + 33, thresholdY - 4);
        gc.setFill(MUTED);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Barres : opérations  •  Ligne : cumul", left, canvas.getHeight() - 8);
    }

    private static void renderDailyLine(Canvas canvas, List<Operation> operations) {
        GraphicsContext gc = prepare(canvas);
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.minusDays(13);
        Map<LocalDate, Integer> dailyCounts = new TreeMap<>();
        for (int i = 0; i < 14; i++) dailyCounts.put(firstDay.plusDays(i), 0);
        for (Operation operation : operations) {
            if (operation == null || operation.getState() == null || operation.getState().getLibelle() != StateOper.VALIDER) continue;
            LocalDate day = operationDate(operation);
            if (day != null && !day.isBefore(firstDay) && !day.isAfter(today)) {
                dailyCounts.computeIfPresent(day, (key, value) -> value + 1);
            }
        }
        int max = Math.max(1, dailyCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0));
        double left = 39, top = 22, plotWidth = canvas.getWidth() - 54, plotHeight = canvas.getHeight() - 68;
        gc.setFont(Font.font("Segoe UI", 9));
        gc.setTextAlign(TextAlignment.RIGHT);
        for (int step = 0; step <= 4; step++) {
            double y = top + plotHeight - plotHeight * step / 4.0;
            gc.setStroke(GRID);
            gc.strokeLine(left, y, left + plotWidth, y);
            gc.setFill(MUTED);
            gc.fillText(String.valueOf(Math.round(max * step / 4.0)), left - 7, y + 3);
        }
        List<Map.Entry<LocalDate, Integer>> points = new ArrayList<>(dailyCounts.entrySet());
        gc.setStroke(BLUE);
        gc.setLineWidth(2.5);
        for (int i = 1; i < points.size(); i++) {
            gc.strokeLine(xAt(i - 1, points.size(), left, plotWidth), yAt(points.get(i - 1).getValue(), max, top, plotHeight),
                    xAt(i, points.size(), left, plotWidth), yAt(points.get(i).getValue(), max, top, plotHeight));
        }
        for (int i = 0; i < points.size(); i++) {
            double x = xAt(i, points.size(), left, plotWidth);
            double y = yAt(points.get(i).getValue(), max, top, plotHeight);
            gc.setFill(BLUE);
            gc.fillOval(x - 3.5, y - 3.5, 7, 7);
            if (i % 2 == 0 || i == points.size() - 1) {
                gc.setFill(MUTED);
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(points.get(i).getKey().format(DateTimeFormatter.ofPattern("dd/MM")), x, top + plotHeight + 17);
            }
        }
        gc.setFill(MUTED);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Nombre d'opérations validées", left, canvas.getHeight() - 8);
    }

    private static void renderSagittal(Canvas canvas, List<Operation> operations) {
        GraphicsContext gc = prepare(canvas);
        Map<String, Integer> flows = new LinkedHashMap<>();
        for (Operation operation : operations) {
            String from = role(operation == null ? null : operation.getExp());
            String to = role(operation == null ? null : operation.getBen());
            if (from == null || to == null) continue;
            String key = from + "|" + to;
            flows.merge(key, 1, Integer::sum);
        }
        List<Map.Entry<String, Integer>> edges = flows.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(8)
                .toList();
        if (edges.isEmpty()) {
            empty(gc, canvas, "Aucun flux entre rôles disponible");
            return;
        }

        double leftX = 12, rightX = canvas.getWidth() - 84, nodeWidth = 72, nodeHeight = 23;
        double startY = 25, gap = (canvas.getHeight() - 53) / ROLES.size();
        Map<String, Double> roleY = new LinkedHashMap<>();
        for (int i = 0; i < ROLES.size(); i++) roleY.put(ROLES.get(i), startY + i * gap + nodeHeight / 2);
        int max = edges.stream().mapToInt(Map.Entry::getValue).max().orElse(1);
        for (Map.Entry<String, Integer> edge : edges) {
            String[] endpoints = edge.getKey().split("\\|", 2);
            Double sourceY = roleY.get(endpoints[0]);
            Double targetY = roleY.get(endpoints[1]);
            if (sourceY == null || targetY == null) continue;
            double lineWidth = 1.3 + 4.5 * edge.getValue() / max;
            gc.setStroke(Color.web("#4385d1", .48));
            gc.setLineWidth(lineWidth);
            gc.beginPath();
            gc.moveTo(leftX + nodeWidth, sourceY);
            gc.bezierCurveTo(leftX + nodeWidth + 82, sourceY, rightX - 82, targetY, rightX, targetY);
            gc.stroke();
            gc.setFill(INK);
            gc.setFont(Font.font("Segoe UI", 9));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(edge.getValue()), (leftX + nodeWidth + rightX) / 2, (sourceY + targetY) / 2 - 4);
        }
        for (int i = 0; i < ROLES.size(); i++) {
            String role = ROLES.get(i);
            double y = startY + i * gap;
            drawRoleNode(gc, leftX, y, nodeWidth, nodeHeight, role);
            drawRoleNode(gc, rightX, y, nodeWidth, nodeHeight, role);
        }
        gc.setFill(MUTED);
        gc.setFont(Font.font("Segoe UI", 8));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Émetteur", leftX, 13);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("Bénéficiaire", rightX + nodeWidth, 13);
    }

    private static void renderMosaic(Canvas canvas, List<Operation> operations) {
        GraphicsContext gc = prepare(canvas);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Operation operation : operations) {
            String from = role(operation == null ? null : operation.getExp());
            String to = role(operation == null ? null : operation.getBen());
            String corridor = corridor(from, to);
            counts.merge(corridor, 1, Integer::sum);
        }
        List<MosaicValue> entries = counts.entrySet().stream()
                .map(entry -> new MosaicValue(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(MosaicValue::value).reversed())
                .toList();
        if (entries.isEmpty()) {
            empty(gc, canvas, "Aucune opération à répartir");
            return;
        }
        double x = 5, y = 5, width = canvas.getWidth() - 10, height = canvas.getHeight() - 10;
        drawTreemap(gc, entries, x, y, width, height, 0);
    }

    private static void drawTreemap(GraphicsContext gc, List<MosaicValue> values, double x, double y,
                                    double width, double height, int depth) {
        if (values.size() == 1) {
            MosaicValue value = values.get(0);
            Color color = PALETTE[depth % PALETTE.length];
            gc.setFill(color);
            gc.fillRoundRect(x + 2, y + 2, Math.max(0, width - 4), Math.max(0, height - 4), 10, 10);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Segoe UI", Math.max(9, Math.min(14, Math.min(width, height) / 8))));
            gc.setTextAlign(TextAlignment.CENTER);
            if (width > 80 && height > 42) {
                gc.fillText(value.label(), x + width / 2, y + height / 2 - 3);
                gc.setFont(Font.font("Segoe UI", 10));
                gc.fillText(value.value() + " opérations", x + width / 2, y + height / 2 + 15);
            } else {
                gc.fillText(value.label() + "  " + value.value(), x + width / 2, y + height / 2 + 4);
            }
            return;
        }
        int total = values.stream().mapToInt(MosaicValue::value).sum();
        int leftTotal = 0, split = 0;
        for (; split < values.size() - 1; split++) {
            leftTotal += values.get(split).value();
            if (leftTotal >= total / 2.0) { split++; break; }
        }
        List<MosaicValue> first = values.subList(0, split);
        List<MosaicValue> second = values.subList(split, values.size());
        int firstTotal = first.stream().mapToInt(MosaicValue::value).sum();
        double ratio = Math.max(.12, Math.min(.88, (double) firstTotal / total));
        if (width >= height) {
            double firstWidth = width * ratio;
            drawTreemap(gc, first, x, y, firstWidth, height, depth);
            drawTreemap(gc, second, x + firstWidth, y, width - firstWidth, height, depth + 1);
        } else {
            double firstHeight = height * ratio;
            drawTreemap(gc, first, x, y, width, firstHeight, depth);
            drawTreemap(gc, second, x, y + firstHeight, width, height - firstHeight, depth + 1);
        }
    }

    private static void drawRoleNode(GraphicsContext gc, double x, double y, double width, double height, String role) {
        gc.setFill(Color.web("#edf4f8"));
        gc.fillRoundRect(x, y, width, height, 8, 8);
        gc.setStroke(Color.web("#c8d8e5"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, width, height, 8, 8);
        gc.setFill(INK);
        gc.setFont(Font.font("Segoe UI", 8));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(role, x + width / 2, y + height / 2 + 3);
    }

    private static String corridor(String from, String to) {
        if (Objects.equals(from, "CLIENT") && Objects.equals(to, "CLIENT")) return "Client → client";
        if (SetOfRoles.isClientAgent(from, to)) return "Client ↔ agent";
        if (SetOfRoles.isAgentOffice(from, to)) return "Agent ↔ office";
        return "Autres parcours";
    }

    private static String role(Accounts account) {
        if (account == null || account.getUser() == null) return null;
        Role role = account.getUser().getRole();
        if (role == null || role.getLibelle() == null) return null;
        String value = role.getLibelle().trim().toUpperCase();
        return ROLES.contains(value) ? value : null;
    }

    private static LocalDate operationDate(Operation operation) {
        try {
            return OffsetDateTime.parse(operation.getDateoperation()).toLocalDate();
        } catch (RuntimeException ignored) {
            try {
                return LocalDate.parse(operation.getDateoperation().substring(0, 10));
            } catch (RuntimeException invalidDate) {
                return null;
            }
        }
    }

    private static double xAt(int index, int count, double left, double width) {
        return left + (count <= 1 ? .5 : (double) index / (count - 1)) * width;
    }

    private static double yAt(int value, int max, double top, double height) {
        return top + height - height * value / max;
    }

    private static GraphicsContext prepare(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), 8, 8);
        return gc;
    }

    private static void empty(GraphicsContext gc, Canvas canvas, String message) {
        gc.setFill(MUTED);
        gc.setFont(Font.font("Segoe UI", 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(message, canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private record MosaicValue(String label, int value) {}

    private static final class SetOfRoles {
        private static boolean isClientAgent(String from, String to) {
            return (Objects.equals(from, "CLIENT") && Objects.equals(to, "AGENT"))
                    || (Objects.equals(from, "AGENT") && Objects.equals(to, "CLIENT"));
        }
        private static boolean isAgentOffice(String from, String to) {
            return (Objects.equals(from, "AGENT") && Objects.equals(to, "OFFICE"))
                    || (Objects.equals(from, "OFFICE") && Objects.equals(to, "AGENT"));
        }
    }
}
