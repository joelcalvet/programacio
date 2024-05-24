package org.example.model.entities;

import java.util.Collection;
import java.util.TreeSet;

public class Ninja {

    private Long id;
    private String nom;
    private double anys;
    private boolean viu;

    private Collection<Poder> poder;


    public Ninja(){}


    public Ninja(long id, String nom, double anys, boolean viu, TreeSet<Poder> poders) {
        this.id = id;
        this.nom = nom;
        this.anys = anys;
        this.viu = viu;
        this.poder = poders;
    }

    public Ninja(String text, double any, boolean selected, TreeSet<Poder> existingPowers) {
        this.nom = text;
        this.anys = any;
        this.viu = selected;
        this.poder = existingPowers;
    }


    public Collection<Poder> getPoder() {
        return poder;
    }

    public void setPoder(Collection<Poder> poder) {
        this.poder = poder;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getAnys() {
        return anys;
    }

    public void setAnys(double anys) {
        this.anys = anys;
    }

    public boolean isViu() {
        return viu;
    }

    public void setViu(boolean viu) {
        this.viu = viu;
    }

    public long getId() {
        return id;
    }

    public void setId(long aLong) {}

    public static class Poder implements Comparable<Poder>{

        private int id;
        private TipusChakra tipusChakra;
        private int quantitatChakra;

        public Poder(TipusChakra chakra, int quantitatChakra) {
            this.tipusChakra = chakra;
            this.quantitatChakra = quantitatChakra;
        }

        public TipusChakra getTipusChakra() {
            return tipusChakra;
        }

        public void setTipusChakra(TipusChakra tipusChakra) {
            this.tipusChakra = tipusChakra;
        }

        public int getQuantitatChakra() {
            return quantitatChakra;
        }

        public void setQuantitatChakra(int quantitatChakra) {
            this.quantitatChakra = quantitatChakra;
        }

        public long getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }


        @Override
        public int compareTo(Poder o) {
            return this.tipusChakra.compareTo(o.getTipusChakra());
        }

        public enum TipusChakra {
            Foc("Katon"), Vent("Kaze"), Llamp("Raiyon"), Terra("Doton"),
            Aigua("Suiton"), Gel("Kori"), Llum("Hikari"), Ombra("Kage"),
            Fusta("Mokuton"), Ferro("Tetsu"), Vapor("Futto"), Pols("Chiri");

            private String nom;

            TipusChakra(String nom) {
                this.nom = nom;
            }

            public String getNom() {
                return nom;
            }

            @Override
            public String toString() {
                return this.name()+" - " +nom;
            }

        }
    }


}

