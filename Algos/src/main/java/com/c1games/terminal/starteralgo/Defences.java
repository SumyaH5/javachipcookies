package com.c1games.terminal.starteralgo;

import com.c1games.terminal.algo.*;
import com.c1games.terminal.algo.io.GameLoop;
import com.c1games.terminal.algo.io.GameLoopDriver;
import com.c1games.terminal.algo.map.GameState;
import com.c1games.terminal.algo.map.MapBounds;
import com.c1games.terminal.algo.map.Unit;
import com.c1games.terminal.algo.units.UnitType;

import java.util.*;

public class Defences {
    
    private final int LAYOUTS = 4;

    // Excess points we have for deciding if we should upgrade
    // dummy values for now
    private final int UPGRADE_SUPPORTS = 8;
    private final int UPGRADE_TURRETS = 10;
    private final int UPGRADE_LAYOUT = 20;

    private ArrayList<Coords> mainTurrets;
    private ArrayList<Coords> mainSupports;

    private ArrayList<ArrayList<Coords>> wallLayout;
    private ArrayList<ArrayList<Coords>> turretLayout;
    private int[] score;
    private int[] cost;

    private ArrayList<Coords> current;
    private int currentLayout;

    private void initMain() {
        mainTurrets.add(new Coords(4, 12));
        mainTurrets.add(new Coords(23, 12));
        mainTurrets.add(new Coords(11, 12));
        mainTurrets.add(new Coords(16, 12));

        mainSupports.add(new Coords(13, 1));
        mainSupports.add(new Coords(14, 1));
    }

    private void initLayouts() {
        // STAGE 0 //
        // CENTER WALL FUNNEL //
        for (int i = 0; i < 7; i++) {
            wallLayout.get(0).add(new Coords(i, 13));
            wallLayout.get(0).add(new Coords(27 - i, 13));
        }

        score[0] = 30;
        cost[0] = 14;

        // STAGE 1 //
        // STAGE 0 + TURRETS // 
        for (int i = 0; i < 7; i++) {
            wallLayout.get(1).add(new Coords(i, 13));
            wallLayout.get(1).add(new Coords(27 - i, 13));
        }

        turretLayout.get(1).add(new Coords(7, 13));
        turretLayout.get(1).add(new Coords(20, 13));

        score[1] = 30;
        cost[1] = 18;

        // STAGE 2 //
        // LEFT WALLS // 
        for (int i = 0; i < 12; i++) {
            wallLayout.get(2).add(new Coords(i, 13));
        }

        score[2] = 30;
        cost[2] = 12;

        // STAGE 3 //
        // RIGHT WALLS // 
        for (int i = 0; i < 12; i++) {
            wallLayout.get(3).add(new Coords(27 - i, 13));
        }

        score[3] = 30;
        cost[3] = 12;
    }

    public Defences() {
        mainTurrets = new ArrayList<Coords>();
        mainSupports = new ArrayList<Coords>();

        current = new ArrayList<Coords>();

        wallLayout = new ArrayList<ArrayList<Coords>>(LAYOUTS);
        turretLayout = new ArrayList<ArrayList<Coords>>(LAYOUTS);

        score = new int[LAYOUTS];
        cost = new int[LAYOUTS];

        for (int i = 0; i < LAYOUTS; i++) {
            ArrayList<Coords> walls = new ArrayList<Coords>();
            ArrayList<Coords> turrets = new ArrayList<Coords>();

            wallLayout.add(walls);
            turretLayout.add(turrets);

            score[i] = 0;
            cost[i] = 0;
        }


        currentLayout = 0;

        initMain();
        initLayouts();
    }

    public void startTurn(GameState state) {
        current.clear();

        deployMain(state);

        int best = 0;
        for (int i = 0; i < LAYOUTS; i++) {
            if (cost[best] > state.data.p1Stats.cores && cost[i] < cost[best]) {
                best = i;
                continue;
            }

            if (cost[i] > state.data.p1Stats.cores) {
                continue;
            }

            if (score[i] > score[best] || (score[i] == score[best] && cost[i] < cost[best])) {
                best = i;
            }
        }

        currentLayout = best;
        deployLayout(state, best);

        int upgrade = 0;    
        while (state.data.p1Stats.cores > UPGRADE_TURRETS && upgrade < mainTurrets.size()) {
            state.attemptUpgrade(mainTurrets.get(upgrade));
        } 

        upgrade = 0;
        while (state.data.p1Stats.cores > UPGRADE_SUPPORTS && upgrade < mainSupports.size()) {
            state.attemptSpawn(mainSupports.get(upgrade), UnitType.Support);
            state.attemptUpgrade(mainSupports.get(upgrade));
        }
         
        upgrade = 0;
        while (state.data.p1Stats.cores > UPGRADE_LAYOUT && upgrade < current.size()) {
            state.attemptUpgrade(current.get(upgrade));
        } 
    }

    public void endTurn(int damage) {
        score[currentLayout] -= damage;
    }

    private void deployMain(GameState state) {
        spawn(state, mainTurrets, UnitType.Turret, false);
    }

    private void deployLayout(GameState state, int layout) {
        spawn(state, wallLayout.get(layout), UnitType.Wall, true);
        spawn(state, turretLayout.get(layout), UnitType.Turret, true);
    
        refund(state);
    }  

    // public int getLayouts() {
    //     return LAYOUTS;
    // }

    private void spawn(GameState state, ArrayList<Coords> spawns, UnitType unit, boolean curr) {
        for (Coords c : spawns) {
            boolean spawned = state.attemptSpawn(c, unit);

            if (spawned && curr) {
                current.add(c);
            }
        }
    }

    private void refund(GameState state) {
        for (Coords c : current) {
            state.attemptRemoveStructure(c);;
        }
    }
}