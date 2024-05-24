package org.example.view;

import org.example.model.entities.Ninja;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ModelComponentsVisuals {

    private DefaultTableModel modelTaulaNinja;
    private DefaultTableModel modelTaulaPoder;
    private ComboBoxModel<Ninja.Poder.TipusChakra> comboBoxModel;

    //Getters


    public ComboBoxModel<Ninja.Poder.TipusChakra> getComboBoxModel() {
        return comboBoxModel;
    }

    public DefaultTableModel getModelTaulaNinja() {
        return modelTaulaNinja;
    }

    public DefaultTableModel getModelTaulaPoder() {
        return modelTaulaPoder;
    }

    public ModelComponentsVisuals() {


        //Anem a definir l'estructura de la taula dels alumnes
        modelTaulaNinja =new DefaultTableModel(new Object[]{"Nom","Anys","Està viu?","Object"},0){
            /**
             * Returns true regardless of parameter values.
             *
             * @param row    the row whose value is to be queried
             * @param column the column whose value is to be queried
             * @return true
             * @see #setValueAt
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }



            //Permet definir el tipo de cada columna
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return Double.class;
                    case 2:
                        return Boolean.class;
                    default:
                        return Object.class;
                }
            }
        };




        //Anem a definir l'estructura de la taula de les matrícules
        modelTaulaPoder =new DefaultTableModel(new Object[]{"Tipus de chakra","Quantitat de chakra"},0){
            /**
             * Returns true regardless of parameter values.
             *
             * @param row    the row whose value is to be queried
             * @param column the column whose value is to be queried
             * @return true
             * @see #setValueAt
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            //Permet definir el tipo de cada columna
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return Ninja.Poder.TipusChakra.class;
                    case 1:
                        return Integer.class;
                    default:
                        return Object.class;
                }
            }
        };



        //Estructura del comboBox
        comboBoxModel=new DefaultComboBoxModel<>(Ninja.Poder.TipusChakra.values());



    }
}
