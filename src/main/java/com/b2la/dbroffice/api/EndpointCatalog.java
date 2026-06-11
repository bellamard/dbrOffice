package com.b2la.dbroffice.api;

import com.b2la.dbroffice.security.Permission;

import java.util.List;

public final class EndpointCatalog {
    private static final String JSON = "application/json";

    private EndpointCatalog() {
    }

    public static List<EndpointDefinition> all() {
        return List.of(
                e("Authentification", "Connexion", "POST", "/auth/login", "{\"username\":\"+243895127236\",\"password\":\"Admin@123\"}", Permission.DASHBOARD_READ),
                e("Authentification", "Envoyer un code de récupération", "POST", "/auth/recovery/send-code", "{\"username\":\"+243810000001\"}", Permission.USERS_WRITE),
                e("Authentification", "Vérifier un code de récupération", "POST", "/auth/recovery/verify-code", "{\"username\":\"+243810000001\",\"code\":\"123456\"}", Permission.USERS_WRITE),
                e("Authentification", "Réinitialiser un mot de passe", "POST", "/auth/recovery/reset-password", "{\"username\":\"+243810000001\",\"code\":\"123456\",\"password\":\"NewPassword@123\"}", Permission.USERS_WRITE),
                e("Utilisateurs", "Créer un client", "POST", "/users", "{\"firstname\":\"Jean\",\"lastname\":\"Mputu\",\"surname\":\"Jean\",\"phone\":\"+243810000001\",\"email\":\"jean@example.com\",\"password\":\"Password@123\",\"datebirth\":\"1995-01-15\"}", Permission.USERS_WRITE),
                e("Utilisateurs", "Créer un agent", "POST", "/users/agent", "{\"firstname\":\"Agent\",\"lastname\":\"DBR\",\"surname\":\"Agent\",\"phone\":\"+243810000002\",\"email\":\"agent@example.com\",\"password\":\"Password@123\",\"datebirth\":\"1990-05-20\"}", Permission.USERS_WRITE),
                e("Utilisateurs", "Créer un provider", "POST", "/users/provider", "{\"firstname\":\"Provider\",\"lastname\":\"DBR\",\"surname\":\"Provider\",\"phone\":\"+243810000003\",\"email\":\"provider@example.com\",\"password\":\"Password@123\",\"datebirth\":\"1988-03-10\"}", Permission.USERS_WRITE),
                e("Utilisateurs", "Lister les utilisateurs", "GET", "/users", "", Permission.USERS_READ),
                e("Utilisateurs", "Afficher un utilisateur", "GET", "/users/{user_id}", "", Permission.USERS_READ),
                e("Utilisateurs", "Modifier un utilisateur", "PUT", "/users/{user_id}", "{\"id\":1,\"firstname\":\"Jean\",\"lastname\":\"Mputu\",\"surname\":\"Jean\",\"phone\":\"+243810000001\",\"email\":\"jean@example.com\",\"datebirth\":\"1995-01-15\",\"role\":{\"libelle\":\"CLIENT\"}}", Permission.USERS_WRITE),
                e("Utilisateurs", "Activer ou désactiver un utilisateur", "PUT", "/users/{user_id}/state", "{\"active\":false}", Permission.USERS_WRITE),
                e("Utilisateurs", "Profil courant", "GET", "/users/profil", "", Permission.USERS_READ),
                e("Utilisateurs", "Ajouter l'adresse courante", "PUT", "/users/address", addressBody("12"), Permission.USERS_WRITE),
                e("Utilisateurs", "Demander un changement de mot de passe", "POST", "/users/modifypwd", "{\"username\":\"+243810000001\"}", Permission.USERS_WRITE),
                e("Utilisateurs", "Définir un nouveau mot de passe", "PUT", "/users/newpwd", "{\"username\":\"+243810000001\",\"code\":\"123456\",\"password\":\"NewPassword@123\"}", Permission.USERS_WRITE),
                e("Utilisateurs", "Activer un compte", "POST", "/users/activation", "{\"username\":\"+243810000001\",\"code\":\"123456\"}", Permission.USERS_WRITE),
                e("Utilisateurs", "Vérifier un username", "POST", "/users/check", "+243810000001", "text/plain", Permission.USERS_READ),
                e("Utilisateurs", "Vérifier les contacts", "POST", "/users/check-contacts", "[\"+243810000001\",\"+243810000002\"]", Permission.USERS_READ),

                e("Comptes", "Créer un compte", "POST", "/accounts", "{\"id\":1,\"name\":\"Dollar americain\",\"signe\":\"USD\"}", Permission.CONFIG_WRITE),
                e("Comptes", "Solde courant", "GET", "/accounts/solde", "", Permission.USERS_READ),
                e("Comptes", "Afficher un compte", "GET", "/accounts/{account_id}", "", Permission.USERS_READ),
                e("Comptes", "Lister les comptes", "GET", "/accounts", "", Permission.USERS_READ),

                e("Adresses", "Créer une adresse", "POST", "/addresses", addressBody("12"), Permission.USERS_WRITE),
                e("Adresses", "Lister les adresses", "GET", "/addresses", "", Permission.USERS_READ),
                e("Adresses", "Afficher une adresse", "GET", "/addresses/{address_id}", "", Permission.USERS_READ),
                e("Adresses", "Modifier une adresse", "PUT", "/addresses/{address_id}", addressBody("14"), Permission.USERS_WRITE),
                e("Adresses", "Valider une adresse", "PUT", "/addresses/check", addressBody("12"), Permission.USERS_WRITE),
                e("Adresses", "Supprimer une adresse", "DELETE", "/addresses/{address_id}", "", Permission.USERS_WRITE),

                e("Configuration", "Créer une devise", "POST", "/devises", "{\"name\":\"Dollar americain\",\"signe\":\"USD\"}", Permission.CONFIG_WRITE),
                e("Configuration", "Lister les devises", "GET", "/devises", "", Permission.CONFIG_READ),
                e("Configuration", "Afficher une devise", "GET", "/devises/{devise_id}", "", Permission.CONFIG_READ),
                e("Configuration", "Modifier une devise", "PUT", "/devises/{devise_id}", "{\"id\":1,\"name\":\"Franc congolais\",\"signe\":\"CDF\"}", Permission.CONFIG_WRITE),
                e("Configuration", "Supprimer une devise", "DELETE", "/devises/{devise_id}", "", Permission.CONFIG_WRITE),
                e("Configuration", "Créer un coût", "POST", "/costs", "{\"devices\":{\"id\":1},\"min\":1,\"max\":1000,\"percent\":2.5}", Permission.CONFIG_WRITE),
                e("Configuration", "Lister les coûts", "GET", "/costs", "", Permission.CONFIG_READ),
                e("Configuration", "Afficher un coût", "GET", "/costs/{cost_id}", "", Permission.CONFIG_READ),
                e("Configuration", "Modifier un coût", "PUT", "/costs/{cost_id}", "{\"devices\":{\"id\":1},\"min\":1,\"max\":2000,\"percent\":3}", Permission.CONFIG_WRITE),
                e("Configuration", "Supprimer un coût", "DELETE", "/costs/{cost_id}", "", Permission.CONFIG_WRITE),
                e("Configuration", "Vérifier un pourcentage", "GET", "/costs/check?value={amount}&deviseId={devise_id}", "", Permission.CONFIG_READ),

                e("Établissements", "Créer un établissement", "POST", "/etablissements", "{\"name\":\"DBR Partner\",\"registrationNumber\":\"RCCM-001\",\"postalAddress\":" + addressBody("1") + ",\"contactDetails\":\"+243810000010\",\"customerType\":\"ENTITES\",\"phoneOwn\":\"+243810000003\"}", Permission.CONFIG_WRITE),
                e("Établissements", "Lister les établissements", "GET", "/etablissements", "", Permission.CONFIG_READ),
                e("Établissements", "Profil établissement", "GET", "/etablissements/profile", "", Permission.CONFIG_READ),
                e("Établissements", "Modifier un établissement", "PUT", "/etablissements", "{\"id\":1,\"nomEtablissement\":\"DBR Partner\",\"idnat\":\"IDNAT-001\",\"rccm\":\"RCCM-001\",\"nif\":\"NIF-001\",\"phone\":\"+243810000010\",\"email\":\"partner@example.com\"}", Permission.CONFIG_WRITE),
                e("Établissements", "Supprimer un établissement", "DELETE", "/etablissements/{etablissement_id}", "", Permission.CONFIG_WRITE),

                e("Opérations", "Créer une opération", "POST", "/operations", "{\"amount\":100,\"phoneBeneficier\":\"+243810000001\",\"typeOperation\":\"DEPOT\",\"devise\":\"USD\"}", Permission.OPERATIONS_WRITE),
                e("Opérations", "Lister les opérations", "GET", "/operations", "", Permission.OPERATIONS_READ),
                e("Opérations", "Afficher par référence", "GET", "/operations/{operation_ref}", "", Permission.OPERATIONS_READ),
                e("Opérations", "Lister les dépôts", "GET", "/operations/depot", "", Permission.OPERATIONS_READ),
                e("Opérations", "Lister les retraits", "GET", "/operations/retrait", "", Permission.OPERATIONS_READ),
                e("Opérations", "Confirmer la clé", "POST", "/operations/clef", "{\"reference\":\"OP-REFERENCE\",\"clef\":\"123456\"}", Permission.OPERATIONS_WRITE),
                e("Opérations", "Confirmer la clé agent", "POST", "/operations/clefCA", "{\"reference\":\"OP-REFERENCE\",\"clef\":\"123456\"}", Permission.OPERATIONS_WRITE),

                e("Conversions", "Créer une conversion", "POST", "/conversions", "{\"amount\":100,\"phoneBeneficier\":\"+243810000001\",\"typeOperation\":\"CONVERSION\",\"devise\":\"USD\",\"etranger\":\"CDF\"}", Permission.OPERATIONS_WRITE),
                e("Conversions", "Lister les conversions", "GET", "/conversions", "", Permission.OPERATIONS_READ),
                e("Conversions", "Afficher une conversion", "GET", "/conversions/{conversion_id}", "", Permission.OPERATIONS_READ),
                e("Conversions", "Conversions entités", "GET", "/conversions/entite", "", Permission.OPERATIONS_READ),
                e("Conversions", "Conversions clients", "GET", "/conversions/client", "", Permission.OPERATIONS_READ),
                e("Conversions", "Confirmer une conversion", "PUT", "/conversions/confirme", "{\"reference\":\"1\",\"clef\":\"123456\"}", Permission.OPERATIONS_WRITE),

                e("Jetons cash", "Créer un jeton", "POST", "/jetons-cash", "{\"firstname\":\"Jean\",\"lastname\":\"Mputu\",\"surname\":\"Junior\",\"address\":\"Kinshasa\",\"phone\":\"+243810000010\",\"email\":\"jean@example.com\",\"amount\":100,\"devise\":\"USD\"}", Permission.OPERATIONS_WRITE),
                e("Jetons cash", "Confirmer la création", "POST", "/jetons-cash/confirmation", "{\"clef\":\"123456\"}", Permission.OPERATIONS_WRITE),
                e("Jetons cash", "Payer un jeton", "POST", "/jetons-cash/paiement", "{\"phone\":\"+243810000010\",\"code\":\"123456\"}", Permission.OPERATIONS_WRITE),
                e("Jetons cash", "Afficher un jeton", "GET", "/jetons-cash/{jeton_cash_reference}", "", Permission.OPERATIONS_READ),
                e("Jetons cash", "Historique des jetons", "GET", "/jetons-cash", "", Permission.OPERATIONS_READ),

                e("KYC", "KYC de l'utilisateur courant", "GET", "/kyc", "", Permission.KYC_READ),
                e("KYC", "Lister les KYC", "GET", "/kyc/list", "", Permission.KYC_READ),
                e("KYC", "Lister les KYC par statut", "GET", "/kyc/list/valider", "", Permission.KYC_READ),
                e("KYC", "Déposer un document KYC", "POST", "/kyc", "{\"typeFile\":\"PASSPORT\",\"filePath\":\"C:\\\\tmp\\\\document.pdf\"}", "multipart/form-data", Permission.KYC_VALIDATE),
                e("KYC", "Modifier un document KYC", "PUT", "/kyc/{kyc_id}", "{\"typeFile\":\"PASSPORT\",\"filePath\":\"C:\\\\tmp\\\\document.pdf\"}", "multipart/form-data", Permission.KYC_VALIDATE),
                e("KYC", "Valider ou refuser un KYC", "PUT", "/kyc/check/{kyc_id}", "{\"stateCarte\":\"VALIDER\",\"typeFile\":\"PASSPORT\",\"reasonCancel\":null}", Permission.KYC_VALIDATE),
                e("KYC", "Supprimer un KYC", "DELETE", "/kyc/{kyc_id}", "", Permission.KYC_VALIDATE),

                e("GPS", "Lister toutes les positions", "GET", "/gps/all", "", Permission.GPS_READ),
                e("GPS", "Positions d'un utilisateur", "GET", "/gps", "{\"id\":1}", Permission.GPS_READ),
                e("GPS", "Créer une position", "POST", "/gps", "{\"user\":{\"id\":1},\"latitude\":-4.325,\"longitude\":15.3222}", Permission.CONFIG_WRITE),

                e("Messagerie", "Envoyer un message", "POST", "/messagerie", "{\"message\":\"Bonjour\",\"exp\":{\"id\":1},\"ben\":{\"id\":2},\"state\":false}", Permission.MESSAGING_WRITE),
                e("Messagerie", "Lister les messages", "GET", "/messagerie", "", Permission.MESSAGING_READ),
                e("Messagerie", "Messages envoyés", "GET", "/messagerie/expedit", "", Permission.MESSAGING_READ),
                e("Messagerie", "Messages reçus", "GET", "/messagerie/benef", "", Permission.MESSAGING_READ),
                e("Messagerie", "Afficher un message", "GET", "/messagerie/{message_id}", "", Permission.MESSAGING_READ),
                e("Messagerie", "Supprimer un message", "DELETE", "/messagerie/{message_id}", "", Permission.MESSAGING_WRITE),

                e("Reporting", "Toutes les actions", "GET", "/reporting/actions", "", Permission.REPORTING_READ),
                e("Reporting", "Actions entre deux dates", "GET", "/reporting/actions/search?start={start}&end={end}", "", Permission.REPORTING_READ),
                e("Reporting", "Actions par utilisateur", "GET", "/reporting/actions/user?username={username}", "", Permission.REPORTING_READ),

                e("Services client", "Transactions", "GET", "/transactions", "", Permission.OPERATIONS_READ),
                e("Services client", "Historiques", "GET", "/historiques", "", Permission.OPERATIONS_READ),
                e("Services client", "Détail d'une opération", "GET", "/insights/{operation_ref}", "", Permission.OPERATIONS_READ),
                e("Services client", "Créer un transfert", "POST", "/transfers", "{\"amount\":100,\"devise\":\"USD\",\"phone\":\"+243895127236\"}", Permission.OPERATIONS_WRITE),
                e("Services client", "Créer un paiement", "POST", "/payments", "{\"amount\":100,\"devise\":\"USD\",\"phone\":\"+243895127236\"}", Permission.OPERATIONS_WRITE),
                e("Services client", "Confirmer un paiement", "POST", "/payments/confirm", "{\"codeReference\":\"OP-REFERENCE\",\"clef\":\"123456\"}", Permission.OPERATIONS_WRITE),
                e("Services client", "Notifications", "GET", "/notifications", "", Permission.MESSAGING_READ),
                e("Services client", "Marquer une notification", "POST", "/state-notification", "{\"id\":\"1\",\"state\":\"READ\"}", Permission.MESSAGING_WRITE),
                e("Services client", "Supprimer une notification", "DELETE", "/notifications/{message_id}", "", Permission.MESSAGING_WRITE),
                e("Services client", "Types de documents", "GET", "/document-types", "", Permission.KYC_READ),
                e("Services client", "Lister les services", "GET", "/services", "", Permission.CONFIG_READ),
                e("Services client", "Afficher un service", "GET", "/services/{etablissement_id}", "", Permission.CONFIG_READ),
                e("Services client", "Lister les convertisseurs", "GET", "/convrtiseurs", "", Permission.CONFIG_READ),
                e("Services client", "Simuler une conversion", "POST", "/convert", "{\"amount\":100,\"deviseSource\":\"USD\",\"deviseDestination\":\"CDF\"}", Permission.OPERATIONS_WRITE),
                e("Services client", "Confirmer la conversion client", "POST", "/confirm-conversion", "{\"codeReference\":\"1\",\"clef\":\"123456\"}", Permission.OPERATIONS_WRITE)
        );
    }

    private static EndpointDefinition e(
            String module, String name, String method, String path, String body, Permission permission) {
        return e(module, name, method, path, body, JSON, permission);
    }

    private static EndpointDefinition e(
            String module, String name, String method, String path, String body, String contentType,
            Permission permission) {
        return new EndpointDefinition(module, name, method, path, body, contentType, permission);
    }

    private static String addressBody(String number) {
        return "{\"number\":\"" + number + "\",\"street\":\"Avenue Lumumba\",\"neighborhood\":\"Gombe\",\"community\":\"Kinshasa\",\"state\":\"Kinshasa\"}";
    }
}
