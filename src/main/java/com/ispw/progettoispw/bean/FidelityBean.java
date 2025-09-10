package com.ispw.progettoispw.bean;

import com.ispw.progettoispw.entity.PrizeOption;
import java.util.List;

public class FidelityBean {

    private int totalPoints;            // punti attuali del cliente
    private String selectedPrizeId;     // "P1" | "P2" | "P3"
    private List<PrizeBean> prizes;   // lista premi da mostrare

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public String getSelectedPrizeId() { return selectedPrizeId; }
    public void setSelectedPrizeId(String selectedPrizeId) { this.selectedPrizeId = selectedPrizeId; }

    public List<PrizeBean> getPrizes() { return prizes; }
    public void setPrizes(List<PrizeBean> prizes) { this.prizes = prizes; }

    public boolean canRedeemSelected() {
        if (selectedPrizeId == null || prizes == null) return false;
        return prizes.stream()
                .filter(p -> p.getId().equals(selectedPrizeId))
                .findFirst()
                .map(p -> totalPoints >= p.getRequiredPoints())
                .orElse(false);
    }


}

