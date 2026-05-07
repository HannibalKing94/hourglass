package org.example.hourglass;

public class HourglassConstants
{
    // XP-Werte
    public static final int XP_PER_LEVEL = 12600;
    public static final int LOSS_XP = 700;
    public static final int WIN_XP_STREAK_0 = 4200;
    public static final int WIN_XP_STREAK_1 = 4675;
    public static final int WIN_XP_STREAK_2 = 5190;
    public static final int WIN_XP_STREAK_3 = 5688;
    public static final int WIN_XP_STREAK_4_PLUS = 6600;

    // Lower-XP
    public static final int LOWER_XP_STREAK_1 = 1100;
    public static final int LOWER_XP_STREAK_2 = 2640;
    public static final int LOWER_XP_STREAK_3 = 4680;
    public static final int LOWER_XP_STREAK_4_BASE = 7800;
    public static final int LOWER_XP_STREAK_4_PLUS_INCREMENT = 3000;

    // Zeit (in Minuten)
    public static final int MINUTES_PER_GAME = 10;
    public static final int MINUTES_PER_LOWER = 10;

    // Privater Konstruktor
    private HourglassConstants() {}
}