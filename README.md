# API de gestion de réservations – Spring Boot

##  Description

Ce projet est une API REST développée avec **Java 17** et **Spring Boot** permettant la gestion de **réservations de ressources partagées** (salles, équipements, etc.).

L’objectif principal est de garantir :

* la **cohérence des données**
* l’absence de **conflits de réservation**
* une bonne gestion de la **concurrence**

---

##  Choix fonctionnels

### Entités principales

* **User**

  * Représente un utilisateur du système
  * Email unique

* **Resource**

  * Représente une ressource réservable
  * Peut être active ou inactive

* **Reservation**

  * Lien entre un utilisateur et une ressource
  * Contient un créneau temporel (`startTime`, `endTime`)
  * Possède un statut (`CONFIRMED`, `CANCELLED`)

---

### Fonctionnalités implémentées

* Création d’un utilisateur
* Consultation des utilisateurs
* Création d’une ressource
* Consultation des ressources
* Création d’une réservation
* Annulation d’une réservation
* Consultation des réservations d’une ressource (disponibilité)

---

##  Règles métier retenues

###  Réservations

* Une réservation doit avoir :

  * `startTime < endTime`
* Impossible de réserver dans le passé
* Une ressource doit être **active** pour être réservée
* Une réservation ne peut pas être **modifiée ou annulée après son début**

---

###  Gestion des conflits

* Deux réservations **ne doivent jamais se chevaucher** pour une même ressource
* Vérification via :

  ```sql
  startTime < endTime AND endTime > startTime
  ```

---

###  Utilisateurs

* L’email est **unique**
* Vérification lors de :

  * création
  * modification (si email changé)

---

##  Arbitrages techniques

###  Architecture

* Architecture en couches :

  * **Controller** → exposition API REST
  * **Service** → logique métier
  * **Repository** → accès base de données

---

###  Base de données

* Utilisation de **H2 (in-memory)** :

  * simple à configurer
  * idéale pour test technique
  * console web intégrée

---

###  Gestion de la concurrence

* Utilisation de :

  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  ```

* Permet :

  * de verrouiller les lignes en base
  * d’éviter les **réservations simultanées conflictuelles**

---

###  Transactions

* Utilisation de :

  ```java
  @Transactional
  ```

* Garantit :

  * cohérence des opérations
  * rollback automatique en cas d’erreur

---

###  DTO

* Séparation entre :

  * entités (JPA)
  * objets d’entrée/sortie (DTO)

* Permet :

  * meilleure sécurité
  * découplage API / modèle interne

---

###  Tests

* Test de concurrence avec :

  * `ExecutorService`
  * `CountDownLatch`

* Objectif :

  * vérifier qu’une seule réservation passe en cas d’accès simultané

---

##  Limites connues

*  Pas d’authentification / autorisation
*  Pas de gestion avancée des fuseaux horaires
*  Pas de validation métier avancée (durée max, quotas, etc.)


---

##  Améliorations possibles

* Ajout de **Spring Security**
* Ajout de cache (Redis)
* Ajouter Swagger (OpenAPI)


---

## ▶️ Lancer le projet

```bash
mvn spring-boot:run
```

---
## Endpoints API
   Users
➕ Créer un utilisateur

POST /users

{
  "name": "Ibrahim",
  "email": "ibrahim@gmail.com"
}
  Récupérer tous les utilisateurs

GET /users

  Récupérer un utilisateur par ID

GET /users/{id}

  Modifier un utilisateur

PUT /users/{id}

{
  "name": "Ibrahim Updated",
  "email": "ibrahim_new@gmail.com"
}
   Resources
➕ Créer une ressource

POST /resources

{
  "name": "Salle A",
  "description": "Salle de réunion",
  "active": true
}
  Récupérer toutes les ressources

GET /resources

  Récupérer une ressource

GET /resources/{id}

  Modifier une ressource

PUT /resources/{id}

{
  "name": "Salle B",
  "description": "Nouvelle salle",
  "active": true
}
  Supprimer une ressource

DELETE /resources/{id}

  Reservations
➕ Créer une réservation

POST /reservations

{
  "userId": "UUID_USER",
  "resourceId": "UUID_RESOURCE",
  "startTime": "2026-04-05T10:00:00",
  "endTime": "2026-04-05T12:00:00"
}
  Annuler une réservation

DELETE /reservations/{id}

  Disponibilité d’une ressource

GET /reservations/resources/{resourceId}/availability

  Comment tester l’api
🔹 Avec Postman
1. Créer un utilisateur
Method : POST
URL : http://localhost:8080/users
Body → JSON
2. Créer une ressource
POST /resources
3. Créer une réservation
POST /reservations
Utiliser les IDs récupérés

## 🌐 Accès

* API : http://localhost:8080/{Endpoint}
* Console H2 : http://localhost:8080/h2-console

---

