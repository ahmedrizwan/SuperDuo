package barqsoft.footballscores.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Links {

    @SerializedName("self")
    @Expose
    private Self self;
    @SerializedName("soccerseason")
    @Expose
    private Soccerseason soccerseason;
    @SerializedName("homeTeam")
    @Expose
    private HomeTeam homeTeam;
    @SerializedName("awayTeam")
    @Expose
    private AwayTeam awayTeam;

    /**
     * @return The self
     */
    public Self getSelf() {
        return self;
    }

    /**
     * @param self The self
     */
    public void setSelf(Self self) {
        this.self = self;
    }

    /**
     * @return The soccerseason
     */
    public Soccerseason getSoccerseason() {
        return soccerseason;
    }

    /**
     * @param soccerseason The soccerseason
     */
    public void setSoccerseason(Soccerseason soccerseason) {
        this.soccerseason = soccerseason;
    }

    /**
     * @return The homeTeam
     */
    public HomeTeam getHomeTeam() {
        return homeTeam;
    }

    /**
     * @param homeTeam The homeTeam
     */
    public void setHomeTeam(HomeTeam homeTeam) {
        this.homeTeam = homeTeam;
    }

    /**
     * @return The awayTeam
     */
    public AwayTeam getAwayTeam() {
        return awayTeam;
    }

    /**
     * @param awayTeam The awayTeam
     */
    public void setAwayTeam(AwayTeam awayTeam) {
        this.awayTeam = awayTeam;
    }

}