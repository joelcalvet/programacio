package org.example.controller;

import org.example.model.entities.Ninja;
import org.example.model.exceptions.DAOException;
import org.example.model.entities.Ninja.Poder;
import org.example.view.ModelComponentsVisuals;
import org.example.model.impls.NinjaDAOJDBCOracleImpl;
import org.example.view.NarutoView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class Controller implements PropertyChangeListener {

    private ModelComponentsVisuals modelComponentsVisuals = new ModelComponentsVisuals();
    private NinjaDAOJDBCOracleImpl dadesNinja;
    private NarutoView view;

    public Controller(NinjaDAOJDBCOracleImpl dadesNinja, NarutoView view) {
        this.dadesNinja = dadesNinja;
        this.view = view;
        canvis.addPropertyChangeListener(this);
        lligaVistaModel();
        afegirListeners();
        view.setVisible(true);
    }

    private void lligaVistaModel() {
        try {
            setModelTaulaNinja(modelComponentsVisuals.getModelTaulaNinja(), dadesNinja.getAll());
        } catch (DAOException e) {
            this.setExcepcio(e);
        }
        JTable taula = view.getTaula();
        taula.setModel(this.modelComponentsVisuals.getModelTaulaNinja());
        taula.getColumnModel().getColumn(3).setMinWidth(0);
        taula.getColumnModel().getColumn(3).setMaxWidth(0);
        taula.getColumnModel().getColumn(3).setPreferredWidth(0);
        JTable taulaPoder = view.getTaulaPoder();
        taulaPoder.setModel(this.modelComponentsVisuals.getModelTaulaPoder());
        view.getComboChakra().setModel(modelComponentsVisuals.getComboBoxModel());
        view.getPestanyes().setEnabledAt(1, false);
        view.getPestanyes().setTitleAt(1, "Poder de ...");
    }

    private void setModelTaulaNinja(DefaultTableModel modelTaulaNinja, List<Ninja> all) {
        modelTaulaNinja.setRowCount(0); // Clear existing rows
        for (Ninja ninja : all) {
            Set<Poder> poders = (Set<Poder>) ninja.getPoder(); // Obtenir els poders directament de l'objecte Ninja
            if (poders == null) {
                System.out.println("Error: No s'han pogut obtenir els poders per al ninja amb ID " + ninja.getId());
                continue;
            }
            modelTaulaNinja.addRow(new Object[]{ninja.getNom(), ninja.getAnys(), ninja.isViu(), ninja, poders});
        }
        SwingUtilities.invokeLater(() -> {
            view.getTaula().setModel(modelTaulaNinja);
            view.getTaula().repaint();
        });
    }

    private void afegirListeners() {
        ModelComponentsVisuals modelo = this.modelComponentsVisuals;
        DefaultTableModel model = modelo.getModelTaulaNinja();
        DefaultTableModel modelPoder = modelo.getModelTaulaPoder();
        JTable taula = view.getTaula();
        JTable taulaPoder = view.getTaulaPoder();
        JTextField campNom = view.getCampNom();
        JTextField campAnys = view.getCampAnys();
        JCheckBox caixaVius = view.getCaixaVius();
        JTabbedPane pestanyes = view.getPestanyes();
        SwingUtilities.invokeLater(this::refrescaTaulaNinjas);

        view.getInsertarButton().addActionListener(e -> {
            if (pestanyes.getSelectedIndex() == 0) {
                if (campNom.getText().isBlank() || campAnys.getText().isBlank()) {
                    JOptionPane.showMessageDialog(null, "Falta omplir alguna dada!!");
                } else {
                    try {
                        NumberFormat num = NumberFormat.getNumberInstance(Locale.getDefault());
                        double any = num.parse(campAnys.getText().trim()).doubleValue();
                        if (any < 0 || any > 121) throw new ParseException("", 0);
                        int selectedRow = view.getTaula().getSelectedRow();

                        TreeSet<Poder> existingPowers = null; // Inicialitzar a null per defecte
                        if (selectedRow == 1) {
                            Ninja selectedNinja = (Ninja) model.getValueAt(selectedRow, 3);
                            existingPowers = (TreeSet<Poder>) selectedNinja.getPoder();
                        }

                        Ninja al = new Ninja(campNom.getText(), any, caixaVius.isSelected(), existingPowers);
                        dadesNinja.save(al);
                        SwingUtilities.invokeLater(this::refrescaTaulaNinjas);
                        campNom.setText("Naruto Uzumaki");
                        campNom.setSelectionStart(0);
                        campNom.setSelectionEnd(campNom.getText().length());
                        campAnys.setText("33");
                        campNom.requestFocus();
                    } catch (ParseException ex) {
                        setExcepcio(new DAOException(3));
                        campAnys.setSelectionStart(0);
                        campAnys.setSelectionEnd(campAnys.getText().length());
                        campAnys.requestFocus();
                    } catch (DAOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else {
                int selectedRow = taula.getSelectedRow();

                Ninja selectedNinja = (Ninja) model.getValueAt(selectedRow, 3);
                if (selectedNinja == null) {
                    JOptionPane.showMessageDialog(null, "Error: No s'ha trobat el Ninja seleccionat.");
                    return;
                }

                Poder.TipusChakra selectedChakra = (Poder.TipusChakra) view.getComboChakra().getSelectedItem();
                if (selectedChakra == null) {
                    JOptionPane.showMessageDialog(null, "Error: Has de seleccionar un tipus de chakra.");
                    return;
                }

                String chakraText = view.getCampChakra().getText();
                if (chakraText.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Error: Has de proporcionar una quantitat de chakra.");
                    return;
                }

                int quantitatChakra;
                try {
                    quantitatChakra = Integer.parseInt(chakraText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Error: La quantitat de chakra ha de ser un número.");
                    return;
                }

                Ninja.Poder newPoder = new Ninja.Poder(selectedChakra, quantitatChakra);
                selectedNinja.getPoder().add(newPoder);
                try {
                    dadesNinja.savePoder(newPoder, selectedNinja.getId());
                } catch (DAOException sqlException) {
                    JOptionPane.showMessageDialog(null, sqlException.getMessage());
                    return;
                }
                ompliPoder(selectedNinja, modelPoder, dadesNinja);
            }
        });

        view.getModificarButton().addActionListener(e -> {
            int filaSel = view.getTaula().getSelectedRow();
            if (filaSel != -1) {
                Ninja ninja = (Ninja) model.getValueAt(filaSel, 3);
                if (view.getPestanyes().getSelectedIndex() == 0) {
                    // Modifica el Ninja
                    ninja.setNom(view.getCampNom().getText());
                    ninja.setAnys(Double.parseDouble(view.getCampAnys().getText().replaceAll(",", ".")));
                    ninja.setViu(view.getCaixaVius().isSelected());
                    try {
                        dadesNinja.update(ninja);
                        SwingUtilities.invokeLater(this::refrescaTaulaNinjas);
                    } catch (DAOException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                } else {
                    // Modifica Poder
                    int filaSelPoder = view.getTaulaPoder().getSelectedRow();
                    if (filaSelPoder != -1) {
                        // Obtenir les dades del poder seleccionat
                        Poder.TipusChakra selectedChakra = (Poder.TipusChakra) view.getComboChakra().getSelectedItem();
                        String chakraText = view.getCampChakra().getText();
                        int quantitatChakra;
                        try {
                            quantitatChakra = Integer.parseInt(chakraText);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Error: La quantitat de chakra ha de ser un número.");
                            return;
                        }
                        // Obtenir la llista de poders del Ninja
                        Set<Poder> poderList;
                        try {
                            poderList = dadesNinja.getPoders(ninja, ninja.getId());
                        } catch (SQLException | DAOException ex) {
                            throw new RuntimeException(ex);
                        }
                        // Obtenir el Poder a l'índex especificat
                        Poder poderToModify = poderList.stream().collect(Collectors.toList()).get(filaSelPoder);
                        if (poderToModify != null) {
                            poderToModify.setTipusChakra(selectedChakra);
                            poderToModify.setQuantitatChakra(quantitatChakra);
                            try {
                                dadesNinja.updatePoder(poderToModify, ninja.getId()); // Actualitzar el poder a la base de dades
                                ompliPoder(ninja, modelPoder, dadesNinja); // Actualitzar la taula de poders
                            } catch (DAOException daoException) {
                                JOptionPane.showMessageDialog(null, daoException.getMessage());
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "No s'ha trobat cap poder per a modificar.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Has de seleccionar un Poder per a modificar-lo.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Has de seleccionar un Ninja per a modificar-lo o modificar el seu Poder.");
            }
        });


        view.getBorrarButton().addActionListener(e -> {
            if (pestanyes.getSelectedIndex() == 0) {
                int filaSel = view.getTaula().getSelectedRow();
                if (filaSel != -1) {
                    Ninja ninja = (Ninja) modelComponentsVisuals.getModelTaulaNinja().getValueAt(filaSel, 3);
                    try {
                        dadesNinja.delete(ninja);
                        SwingUtilities.invokeLater(this::refrescaTaulaNinjas); // Refresca la taula en un nou fil d'execució
                    } catch (DAOException daoException) {
                        JOptionPane.showMessageDialog(null, daoException.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Has de seleccionar un Ninja per a borrar-lo.");
                }
            } else {
                int filaSel = view.getTaulaPoder().getSelectedRow();
                if (filaSel != -1) {
                    int filaSelNinja = view.getTaula().getSelectedRow();
                    Ninja ninja = (Ninja) modelComponentsVisuals.getModelTaulaNinja().getValueAt(filaSelNinja, 3);
                    Set<Poder> poderList;
                    try {
                        poderList = dadesNinja.getPoders(ninja, ninja.getId());
                    } catch (SQLException | DAOException ex) {
                        throw new RuntimeException(ex);
                    }
                    Poder poderToDelete = poderList.stream().collect(Collectors.toList()).get(filaSel);
                    if (poderToDelete != null) {
                        ninja.getPoder().remove(poderToDelete);
                        try {
                            dadesNinja.deletePoder(poderToDelete, ninja.getId()); // Delete the poder from the database
                            ompliPoder(ninja, modelPoder, dadesNinja); // Refresh the poder table
                        } catch (DAOException daoException) {
                            JOptionPane.showMessageDialog(null, daoException.getMessage());
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "No s'ha trobat cap poder per a borrar.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Has de seleccionar un Poder per a borrar-lo.");
                }
            }
        });

        taula.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int filaSel = taula.getSelectedRow();
                if (filaSel != -1) {
                    Ninja ninja = (Ninja) model.getValueAt(filaSel, 3);
                    campNom.setText(ninja.getNom());
                    campAnys.setText(String.valueOf(ninja.getAnys()).replace(".", ","));
                    caixaVius.setSelected(ninja.isViu());
                    view.getPestanyes().setEnabledAt(1, true);
                    view.getPestanyes().setTitleAt(1, "Poder de " + campNom.getText());
                    ompliPoder(ninja, modelPoder, dadesNinja);
                } else {
                    campNom.setText("");
                    campAnys.setText("");
                    view.getPestanyes().setEnabledAt(1, false);
                    view.getPestanyes().setTitleAt(1, "Poder de ...");
                }
            }
        });
    }

    private void refrescaTaulaNinjas() {
        try {
            setModelTaulaNinja(modelComponentsVisuals.getModelTaulaNinja(), dadesNinja.getAll());
        } catch (DAOException e) {
            setExcepcio(e);
        }
    }

    private static void ompliPoder(Ninja ni, DefaultTableModel modelPoder, NinjaDAOJDBCOracleImpl dadesNinja) {
        if (ni == null) return; // Comprovació de nul·litat
        modelPoder.setRowCount(0);
        try {
            Set<Poder> poders = dadesNinja.getPoders(ni, ni.getId());
            for (Poder poder : poders) {
                modelPoder.addRow(new Object[]{poder.getTipusChakra(), poder.getQuantitatChakra()});
            }
        } catch (DAOException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String PROP_EXCEPCIO = "excepcio";
    private DAOException excepcio;

    public DAOException getExcepcio() {
        return excepcio;
    }

    public void setExcepcio(DAOException excepcio) {
        DAOException valorVell = this.excepcio;
        this.excepcio = excepcio;
        canvis.firePropertyChange(PROP_EXCEPCIO, valorVell, excepcio);
    }

    PropertyChangeSupport canvis = new PropertyChangeSupport(this);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        DAOException rebuda = (DAOException) evt.getNewValue();
        try {
            throw rebuda;
        } catch (DAOException e) {
            switch (evt.getPropertyName()) {
                case PROP_EXCEPCIO:
                    switch (rebuda.getTipo()) {
                        case 0:
                            JOptionPane.showMessageDialog(null, rebuda.getMessage());
                            System.exit(1);
                            break;
                        case 1:
                            JOptionPane.showMessageDialog(null, rebuda.getMessage());
                            break;
                        case 2:
                            JOptionPane.showMessageDialog(null, rebuda.getMessage());
                            view.getCampNom().setSelectionStart(0);
                            view.getCampNom().setSelectionEnd(view.getCampNom().getText().length());
                            view.getCampNom().requestFocus();
                            break;
                    }
            }
        }
    }
}
