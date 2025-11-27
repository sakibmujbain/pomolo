package pages.pomodoro;

import com.Main;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PomodoroController {

    @FXML private Label timerLabel;
    @FXML private Button togglePlayPauseButton, stopButton, pomodoroButton;
    @FXML private FontIcon playPauseIcon, pomodoroIcon;
    @FXML private Circle timerProgressRing;
    @FXML private StackPane ringVisualsStack;
    @FXML private Label currentDayTimeLabel;
    @FXML private BarChart<String, Number> pomodoroBarChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label updateIndicator;
    @FXML private Button increaseHourButton, decreaseHourButton, increaseMinuteButton, decreaseMinuteButton;
    @FXML private HBox increaseButtonHBox, decreaseButtonHBox;

    private Timeline timeline;
    private int timeSeconds = 25 * 60; // Default to 25 minutes
    private final StringProperty timerText = new SimpleStringProperty();
    private Timer dbUpdateTimer;
    private boolean isPomodoroMode = true; // true for Pomodoro, false for Stopwatch
    private boolean isRunning = false;
    private long lastRecordedTime = -1;
    private static final String POMODORO_ICON_CODE = "fas-carrot";
    private static final String STOPWATCH_ICON_CODE = "fas-stopwatch";

    @FXML
    private void initialize() {
        timerLabel.textProperty().bind(timerText);
        updateTimerLabel();
        timerProgressRing.setManaged(false);
        ringVisualsStack.setManaged(false);
        updateIndicator.setOpacity(0);

        // Set up the chart
        xAxis.setCategories(FXCollections.observableArrayList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
        yAxis.setLabel("Minutes");
        loadChartData();

        // Set initial button states
        stopButton.setDisable(true);
        increaseButtonHBox.setDisable(false);
        decreaseButtonHBox.setDisable(false);
    }

    @FXML
    private void togglePlayPause() {
        if (isRunning) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        isRunning = true;
        playPauseIcon.setIconLiteral("fas-pause");
        stopButton.setDisable(false);
        increaseButtonHBox.setDisable(true);
        decreaseButtonHBox.setDisable(true);

        if (isPomodoroMode) {
            if (timeline == null) {
                timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                    timeSeconds--;
                    updateTimerLabel();
                    updateProgressRing();
                    if (timeSeconds <= 0) {
                        timeline.stop();
                        playSound();
                        // Optionally reset or handle completion
                    }
                }));
                timeline.setCycleCount(Timeline.INDEFINITE);
            }
            timeline.play();
            startDatabaseUpdateTimer();
        } else { // Stopwatch mode
            if (timeline == null) {
                timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                    timeSeconds++;
                    updateTimerLabel();
                }));
                timeline.setCycleCount(Timeline.INDEFINITE);
            }
            timeline.play();
        }
    }

    private void pauseTimer() {
        isRunning = false;
        if (timeline != null) {
            timeline.pause();
        }
        playPauseIcon.setIconLiteral("fas-play");
        if (isPomodoroMode) {
            stopDatabaseUpdateTimer();
        }
    }

    @FXML
    private void stopTimer() {
        isRunning = false;
        if (timeline != null) {
            timeline.stop();
        }
        timeSeconds = isPomodoroMode ? 25 * 60 : 0;
        updateTimerLabel();
        updateProgressRing();
        playPauseIcon.setIconLiteral("fas-play");
        stopButton.setDisable(true);
        increaseButtonHBox.setDisable(false);
        decreaseButtonHBox.setDisable(false);

        if (isPomodoroMode) {
            stopDatabaseUpdateTimer();
            // Force one final update on stop
            if (lastRecordedTime != -1) {
                long elapsedSeconds = (System.currentTimeMillis() - lastRecordedTime) / 1000;
                // PomodoroDB.addFocusTime(elapsedSeconds);
                lastRecordedTime = -1; // Reset
                System.out.println("Final focus time recorded on stop.");
                flashUpdateIndicator();
                loadChartData();
            }
        }
    }

    @FXML
    private void toggleMode() {
        if (isRunning) return; // Do not allow mode switching while timer is running

        isPomodoroMode = !isPomodoroMode;
        if (isPomodoroMode) {
            pomodoroIcon.setIconLiteral(STOPWATCH_ICON_CODE);
            pomodoroButton.setText("Stopwatch");
            timeSeconds = 25 * 60; // Reset to Pomodoro default
            timerProgressRing.setManaged(false);
            ringVisualsStack.setManaged(false);
            currentDayTimeLabel.setVisible(true);
            pomodoroBarChart.setVisible(true);
            increaseButtonHBox.setVisible(true);
            decreaseButtonHBox.setVisible(true);
        } else { // Stopwatch mode
            pomodoroIcon.setIconLiteral(POMODORO_ICON_CODE);
            pomodoroButton.setText("Pomodoro");
            timeSeconds = 0; // Reset to Stopwatch default
            timerProgressRing.setManaged(false);
            ringVisualsStack.setManaged(false);
            currentDayTimeLabel.setVisible(false);
            pomodoroBarChart.setVisible(false);
            increaseButtonHBox.setVisible(false);
            decreaseButtonHBox.setVisible(false);
        }
        updateTimerLabel();
        updateProgressRing();
    }

    private void updateTimerLabel() {
        int hours = timeSeconds / 3600;
        int minutes = (timeSeconds % 3600) / 60;
        int seconds = timeSeconds % 60;
        timerText.set(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void updateProgressRing() {
        if (isPomodoroMode) {
            double progress = 1.0 - (double) timeSeconds / (25.0 * 60.0);
            timerProgressRing.setStrokeDashOffset(314 * progress); // 314 is approx circumference (100 * PI)
        } else {
            timerProgressRing.setStrokeDashOffset(314); // Hide progress in stopwatch mode
        }
    }

    private void playSound() {
        try {
            URL resource = getClass().getResource("/1_second_tone.mp3");
            if (resource == null) {
                System.err.println("Sound file not found!");
                return;
            }
            Media sound = new Media(resource.toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDatabaseUpdateTimer() {
        if (dbUpdateTimer != null) {
            dbUpdateTimer.cancel();
        }
        dbUpdateTimer = new Timer(true); // Run as a daemon thread
        lastRecordedTime = System.currentTimeMillis();

        dbUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isRunning) {
                    cancel();
                    return;
                }
                long currentTime = System.currentTimeMillis();
                long elapsedSeconds = (currentTime - lastRecordedTime) / 1000;
                lastRecordedTime = currentTime;

                // Update database on the JavaFX application thread
                Platform.runLater(() -> {
                    // PomodoroDB.addFocusTime(elapsedSeconds);
                    System.out.println(elapsedSeconds + " seconds of focus time recorded.");
                    flashUpdateIndicator();
                    loadChartData(); // Refresh chart data
                });
            }
        }, 5000, 5000); // Wait 5 seconds, then run every 5 seconds
    }

    private void stopDatabaseUpdateTimer() {
        if (dbUpdateTimer != null) {
            dbUpdateTimer.cancel();
            dbUpdateTimer = null;
        }
    }

    private void loadChartData() {
        // PomodoroDB.createTable(); // Ensure table exists
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Map<String, Number> weeklyData = new HashMap<>();
        // for (int i = 0; i < 7; i++) {
        //     LocalDate day = startOfWeek.plusDays(i);
        //     long totalSeconds = PomodoroDB.getTimeForDate(day.toString());
        //     double totalMinutes = totalSeconds / 60.0;
        //     String dayAbbreviation = day.format(DateTimeFormatter.ofPattern("EEE"));
        //     weeklyData.put(dayAbbreviation, totalMinutes);
        // }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();

        // Add data in the correct order for the chart
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            data.add(new XYChart.Data<>(day, weeklyData.getOrDefault(day, 0)));
        }
        series.setData(data);

        Platform.runLater(() -> {
            pomodoroBarChart.getData().setAll(series);
            // Also update the current day's total time label
            long todaySeconds = 0; // PomodoroDB.getTimeForDate(today.toString());
            currentDayTimeLabel.setText(String.format("Today: %.1f minutes", todaySeconds / 60.0));
        });
    }

    private void flashUpdateIndicator() {
        Platform.runLater(() -> {
            updateIndicator.setOpacity(1);
            // Create a fade out transition
            Timeline fadeTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), event -> updateIndicator.setOpacity(0))
            );
            fadeTimeline.play();
        });
    }

    @FXML
    private void increaseTime() {
        if (isRunning) return;
        timeSeconds += 60; // Increase by 1 minute
        updateTimerLabel();
    }

    @FXML
    private void decreaseTime() {
        if (isRunning) return;
        if (timeSeconds > 60) {
            timeSeconds -= 60; // Decrease by 1 minute
        } else {
            timeSeconds = 0;
        }
        updateTimerLabel();
    }

    @FXML
    private void goToHome() throws IOException {
        Parent home = FXMLLoader.load(Main.class.getResource("/pages/home/home.fxml"));
        Main.getMainStage().getScene().setRoot(home);
    }
}