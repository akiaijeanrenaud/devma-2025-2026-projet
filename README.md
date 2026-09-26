# EcoBudget 🌿

Dépôt de base pour le projet du cours de développement mobile avancé.

## Étapes de réalisation du projet

1. **Initialisation du dépôt**
   - Création du projet Android (Kotlin, Gradle Kotlin DSL) à partir du dépôt de base fourni par le professeur.
   - Configuration du `.gitignore` pour exclure les fichiers générés par l'IDE et le build (`.idea`, `.gradle`, `build/`, `local.properties`).

2. **Conception du modèle de données**
   - Définition de `Transaction` (id, titre, montant, type, catégorie, date, note) et `TransactionType` (`INCOME` / `EXPENSE`).
   - Définition de `Category` (énumération avec libellé et emoji) et `YearMonth` (mois sélectionné, navigation mois précédent/suivant).

3. **Mise en place de l'architecture MVVM**
   - Création de `TransactionRepository` (interface) et `FakeTransactionRepository` (implémentation en mémoire pour simuler les données) afin de découpler la source de données de la logique métier.
   - Création d'`EcoBudgetViewModel`, qui expose l'état de l'écran via un `StateFlow<EcoBudgetUiState>` combiné à partir du mois sélectionné, des transactions, des filtres de catégories et de l'état des dialogues.

4. **Extraction en module partagé Kotlin Multiplatform (`shared`)**
   - Création du module `shared` (plugin `kotlin("multiplatform")`) avec cibles `androidTarget` et iOS (`iosX64`, `iosArm64`, `iosSimulatorArm64`).
   - Déplacement des modèles, du repository et du `ViewModel` dans `commonMain` pour pouvoir réutiliser la même logique métier sur Android et iOS.
   - Ajout d'implémentations spécifiques par plateforme (`UUID.android.kt`, `UUID.ios.kt`) via le mécanisme `expect`/`actual`.
   - Ajout du module `shared` comme dépendance du module `app` (`implementation(project(":shared"))`).

5. **Logique métier du budget**
   - Calcul du revenu total, des dépenses totales et du solde à partir des transactions du mois affiché.
   - Calcul du taux d'utilisation du budget mensuel fixe (500 000 F CFA) et du budget restant.
   - Filtrage des transactions par catégorie sélectionnée.

6. **Construction de l'interface utilisateur (Jetpack Compose)**
   - `EcoBudgetScreen` : écran principal assemblant le résumé du budget, la navigation mensuelle et la liste des transactions.
   - `MonthNavigatorBar` : navigation entre les mois (précédent / suivant / retour au mois courant).
   - `TransactionCard` : affichage d'une transaction (titre, montant, catégorie, date).
   - `AddTransactionDialog` : formulaire d'ajout et de modification d'une transaction.
   - Thème de l'application (`Color.kt`, `Theme.kt`, `Type.kt`) pour une interface Material 3 cohérente.

7. **Câblage dans `MainActivity`**
   - Instanciation du `ViewModel` via `by viewModels()` et affichage d'`EcoBudgetScreen` dans un `Surface` avec le thème de l'application.

8. **Tests**
   - Ajout de tests unitaires (`ExampleUnitTest`, `ExampleRobolectricTest`) et d'un test instrumenté (`ExampleInstrumentedTest`) pour vérifier le bon fonctionnement des composants.

9. **Configuration du build et de la signature**
   - Ajout d'une configuration de signature (`signingConfigs`) pour le build de release, avec lecture du keystore et des mots de passe via variables d'environnement (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`).

10. **Publication sur GitHub personnel**
    - Fork du dépôt d'origine du professeur sur un compte GitHub personnel.
    - Mise à jour du fork avec les modifications du projet (module `shared`, écrans, ViewModel) via `git add`, `git commit`, `git push`.
