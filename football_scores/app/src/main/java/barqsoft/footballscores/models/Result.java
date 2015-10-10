package barqsoft.footballscores.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("goalsHomeTeam")
    @Expose
    private int goalsHomeTeam;
    @SerializedName("goalsAwayTeam")
    @Expose
    private int goalsAwayTeam;

    /**
     * @return The goalsHomeTeam
     */
    public int getGoalsHomeTeam() {
        return goalsHomeTeam;
    }

    /**
     * @param goalsHomeTeam The goalsHomeTeam
     */
    public void setGoalsHomeTeam(int goalsHomeTeam) {
        this.goalsHomeTeam = goalsHomeTeam;
    }

    /**
     * @return The goalsAwayTeam
     */
    public int getGoalsAwayTeam() {
        return goalsAwayTeam;
    }

    /**
     * @param goalsAwayTeam The goalsAwayTeam
     */
    public void setGoalsAwayTeam(int goalsAwayTeam) {
        this.goalsAwayTeam = goalsAwayTeam;
    }

}