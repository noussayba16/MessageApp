package main.java.com.ubo.tp.message.controller;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.session.Session;
import main.java.com.ubo.tp.message.datamodel.User;

public class LoginController {

    private final DataManager dataManager;
    private final Session session;

    public LoginController(DataManager dataManager, Session session) {
        this.dataManager = dataManager;
        this.session = session;
    }

    // =========================
    // CREATION DE COMPTE
    // =========================
    public String register(String tag, String name, String password) {

        if (tag == null || tag.isEmpty() || name == null || name.isEmpty()) {
            return "Le tag et le nom sont obligatoires.";
        }

        if (dataManager.getUser(tag) != null) {
            return "Ce tag existe déjà.";
        }

        dataManager.createUser(tag, name, password);

        return "SUCCESS";
    }

    // =========================
    // CONNEXION
    // =========================
    public String login(String tag, String password) {

        if (tag == null || tag.isEmpty()) {
            return "Veuillez saisir un tag.";
        }

        User user = dataManager.getUser(tag);

        if (user == null) {
            return "Utilisateur introuvable.";
        }

        if (user.getUserPassword() != null &&
                !user.getUserPassword().equals(password)) {
            return "Mot de passe incorrect.";
        }

        user.setOnline(true);
        dataManager.modifyUser(user.getUuid(), user);

        // MAJ SESSION
        session.connect(user);

        return "SUCCESS";
    }
}
