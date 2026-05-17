# Golden Path Monorepo

Ce projet est structuré sous forme de **monorepo Maven**. Il vise à consolider le développement, la gestion des dépendances, et le déploiement continu d'un ensemble de microservices et de librairies partagées, tout en garantissant des temps de build optimisés.

Ce fichier README documente les trois aspects fondamentaux du projet : l'architecture, l'optimisation des builds avec GIB, et l'intégration continue via Jenkins.

---

## 1. Architecture du Projet (Services + Shared Libs)

Le projet adopte une approche multi-modules (monorepo), orchestrée par un `pom.xml` parent à la racine (`com.awb.goldenpath:monorepo`). 

Cette architecture est divisée en deux répertoires principaux :

*   **`/services/`** : Ce répertoire contient les différents microservices métiers de l'application. Chaque service est un module Maven indépendant. On y retrouve par exemple :
    *   `service-notification`
    *   `service-order`
    *   `service-payment`
    *   `service-product`
    *   `service-user`
*   **`/shared-libs/`** : Ce répertoire regroupe les bibliothèques communes ou "shared libraries" (`shared-lib`, `shared-lib-2`). Ces modules contiennent du code réutilisable (utilitaires, modèles de données communs, configuration de sécurité) qui est partagé et consommé comme dépendance par les différents services.



## 2. Intégration de GIB (Gitflow Incremental Builder)

Pour éviter de recompiler l'intégralité du monorepo à chaque modification (ce qui serait très chronophage), le projet intègre **Gitflow Incremental Builder (GIB)**. 


*   **`.mvn/extensions.xml`** : Déclare l'extension GIB (`io.github.gitflow-incremental-builder`) pour qu'elle s'intègre directement au cycle de vie de Maven.
*   **`.mvn/maven.config`** : Centralise toute la configuration du comportement incrémental. On y retrouve les paramètres clés :
    *   `-Dgib.buildDownstream=always` : Si une `shared-lib` est modifiée, GIB forcera le build de tous les `services` qui en dépendent.
    *   `-Dgib.buildUpstream=always` : S'assure que les dépendances parentes nécessaires sont bien prises en compte.
    *   `-Dgib.excludePathsMatching=.*\.md|Jenkinsfile|\.gitignore` : Évite de déclencher des builds inutiles si on modifie uniquement la documentation ou le pipeline.


---

## 3. Logique du Jenkinsfile pour le Build des Modules Nécessaires

Le pipeline d'Intégration et de Déploiement Continus (CI/CD) est défini dans le fichier `Jenkinsfile` à la racine via un **Pipeline Déclaratif**.

L'objectif principal du pipeline est de tirer parti de la structure du monorepo et de GIB pour faire du **Build Sélectif (Incremental Build)**, tout en gérant le versioning de manière dynamique.

**La logique est articulée autour de plusieurs points clés :**

1.  **Calcul Dynamique de la Branche de Référence (`resolveGibArgs()`) :**
    Au lieu de toujours comparer avec le commit précédent, le pipeline analyse le contexte Git pour optimiser le build :
    *   S'il y a un historique de build réussi (`GIT_PREVIOUS_SUCCESSFUL_COMMIT`), il ne compile que ce qui a changé *depuis ce dernier succès*.
    *   S'il n'y a pas d'historique (ex: nouvelle branche), il applique des règles de Gitflow (les branches `feature` ou `release` se comparent à `develop`, les `hotfix` à `main`).
    Cette logique génère les arguments `GIB_ARGS` injectés dans Maven.

3.  **Exécution Sélective par Étapes (Stages) :**
    *   **Build & Tests :** Maven est lancé (`mvn clean install` puis `mvn test`) avec les variables `GIB_ARGS`. Grâce à GIB, Maven ignorera tous les modules qui n'ont pas été modifiés.
    *   **Déploiement Nexus :** Les artefacts compilés sont poussés sur Nexus (`mvn deploy`), mais uniquement depuis les branches principales (`develop`, `main`, `master`).
    *   **Conteneurisation (Jib) :** La création et la publication des images Docker (`mvn jib:build`) sont réservées aux branches importantes (`main`, `develop`, `release/*`, `hotfix/*`). Là encore, seules les images des microservices modifiés sont construites et poussées vers la registry.


