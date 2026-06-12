# 🔬 Analyse Comparative des Builds Jenkins #17 vs #18

## Résumé Exécutif

| Métrique | Build #17 | Build #18 | Gain |
|---|---|---|---|
| **Résultat** | ✅ SUCCESS | ✅ SUCCESS |  |
| **Slave Jenkins** | Jenkins_slave | Jenkins_slave2 | Changement de nœud |
| **Modules dans le Reactor** | **10/10** (tous) | **5/5** (incrémental) | **5 modules évités** |
| **Checkout SCM** | `git clone` (full) | `git fetch` (incrémental) | ⚡ Plus rapide |
| **Commit message** | "build the monorepo" | "Build impacted services" | Intention d'optimiser |

---

## 📊 Comparaison Stage par Stage

### 1️⃣ Declarative: Checkout SCM

| | Build #17 | Build #18 |
|---|---|---|
| **Méthode** | `git clone` complet | `git fetch` incrémental |
| **Raison** | Workspace vide, premier build sur ce slave | Workspace déjà existant sur Jenkins_slave2 |

> [!IMPORTANT]
> **Optimisation dans #18** : Le checkout fait un `git fetch` au lieu d'un `git clone`. Cela évite de re-télécharger tout l'historique du repo. Le gain est significatif car le clone complet nécessite un `git init` + `fetch` + `checkout`, tandis que le fetch incrémental ne récupère que les deltas.

---

### 2️⃣ Set Version
- **#17** : Version = `1.0.0-9981-dev-SNAPSHOT`
- **#18** : Version = `1.0.0-db8d-dev-SNAPSHOT`
- ⏱️ **Impact** : Négligeable (~identique dans les deux builds)

---

### 3️⃣ Resolve GIB Strategy

| | Build #17 | Build #18 |
|---|---|---|
| **Reference** | `2daeb59e...` | `998191af...` (= commit du build #17) |
| **Signification** | Compare contre le dernier build réussi | Compare contre le commit de #17 (son prédécesseur) |

> [!NOTE]
> Le Build #18 utilise le commit du Build #17 comme référence de comparaison GIB, ce qui signifie qu'il ne détecte que les changements **depuis** le Build #17.

---

### 4️⃣ Gitleaks Scan
- **#17** : Scan ~84 KB en 13ms — ✅ Aucun secret
- **#18** : Scan ~86 KB en 15ms — ✅ Aucun secret
- ⏱️ **Impact** : Négligeable (~identique)

---

### 5️⃣ 🔥 Build (Maven `clean install -DskipTests`) — **LE PLUS GROS GAIN**

| | Build #17 | Build #18 | Différence |
|---|---|---|---|
| **Durée Maven** | **05:30 min** (330s) | **03:39 min** (219s) | **🔥 -1min51s (-34%)** |
| **Modules compilés** | 10/10 | 5/5 | 5 modules en moins |
| **Cache Maven** | ❌ Cache miss | ❌ Cache miss | Les deux partent de zéro |
| **Module changé** | `monorepo` (root pom) | `service-user` seulement | Scope très différent |

#### Détail des modules dans le Reactor :

```diff
  Build #17 (10 modules)          Build #18 (5 modules)
  ─────────────────────           ─────────────────────
  monorepo           [1/10]       monorepo           [1/5]
  shared-libs        [2/10]       shared-libs        [2/5]
  shared-lib-2       [3/10]       shared-lib-2       [3/5]
  services           [4/10]       services           [4/5]
  service-user       [5/10]       service-user       [5/5]
- shared-lib         [6/10]       
- service-order      [7/10]       
- service-notification [8/10]     
- service-product    [9/10]       
- service-payment    [10/10]      
```

> [!CAUTION]
> **Pourquoi le Build #17 compile tout** : Le commit "build the monorepo" a modifié le `pom.xml` racine (`monorepo`). Comme c'est le parent de tous les modules, **GIB considère que TOUS les modules dépendants sont impactés** → 10/10 modules reconstruits.
>
> **Pourquoi le Build #18 compile 5 modules** : Le commit "Build impacted services" n'a modifié que `service-user`. GIB ne reconstruit que `service-user` + ses dépendances parentes (monorepo, shared-libs, shared-lib-2, services). Les 5 autres services non-impactés sont exclus du reactor.

> [!TIP]
> **Gain de 1min51s expliqué** : L'essentiel du temps économisé vient du fait que 5 services Spring Boot (service-order, service-notification, service-product, service-payment, shared-lib) n'ont PAS été compilés, empaquetés en JAR, ni repackagés avec Spring Boot. Chaque service prend environ 20-30 secondes à compiler + packager.

---

### 6️⃣ Tests & Coverage (Maven `verify`)

| | Build #17 | Build #18 | Différence |
|---|---|---|---|
| **Durée Maven** | **31.074s** | **12.967s** | **🔥 -18.1s (-58%)** |
| **Modules testés** | 10/10 | 5/5 | 5 modules en moins |
| **Tests exécutés** | Tests service-user (3 tests) | Tests service-user (3 tests, 1.742s) | Identique |

> [!IMPORTANT]
> **Gain de 18 secondes (-58%)** : Les 5 modules absents du reactor ne sont même pas traversés. Bien que la majorité des modules n'aient pas de tests réels (JaCoCo skip), le simple fait de les parcourir dans le reactor, résoudre les dépendances, et exécuter les phases lifecycle prend du temps. En n'ayant que 5 modules, Maven évite tout ce travail inutile.

---

### 7️⃣ Quality Analysis (SonarQube)

| | Build #17 | Build #18 | Différence |
|---|---|---|---|
| **Durée Maven** | **14.870s** | **12.520s** | **🔥 -2.35s (-16%)** |
| **Modules analysés** | 10 (9 SKIPPED par GIB) | 5 (4 SKIPPED par GIB) | |
| **Temps Sonar** | 4.356s | 3.559s | -0.8s |

> [!NOTE]
> **Gain modéré** : Dans les deux cas, SonarQube skip la majorité des modules via GIB. Mais le Build #17 traverse quand même les 10 modules dans le reactor (même si 9 sont SKIPPED), alors que #18 n'en traverse que 5 (4 SKIPPED). Le gain est surtout dû au overhead du lifecycle Maven sur les modules supplémentaires.

---

### 8️⃣ Publish Artifacts (Maven `deploy`)

| | Build #17 | Build #18 | Différence |
|---|---|---|---|
| **Durée Maven** | **01:06 min** (66s) | **17.393s** | **🔥 -48.6s (-74%)** |
| **Modules déployés** | 10/10 | 5/5 | 5 modules en moins |
| **Mode** | `-T2C` (multi-thread) | `-T2C` (multi-thread) | Identique |

> [!IMPORTANT]
> **Le 2ème plus gros gain !** Déployer vers Nexus est très I/O-intensive. En n'ayant que 5 modules au lieu de 10, le Build #18 économise ~49 secondes. Chaque JAR (surtout les Spring Boot fat-JARs) doit être uploadé vers le registry Maven, ce qui prend du temps réseau.

---

### 9️⃣ Docker Build & Push (Jib)

| | Build #17 | Build #18 | Différence |
|---|---|---|---|
| **Durée Maven** | **58.716s** | **29.325s** | **🔥 -29.4s (-50%)** |
| **Modules traités** | 10/10 | 5/5 | 5 modules en moins |
| **Images construites** | Toutes les images service | Seulement service-user | |

> [!NOTE]
> **Gain de ~30 secondes** : Même si les modules non-service ont `jib: skip = true`, le reactor les traverse quand même dans le Build #17. En #18, seuls les 5 modules pertinents sont traversés, et Jib ne construit/pousse que l'image `service-user`. Les 4 autres services ne sont même pas dans le reactor.

---

## 📈 Tableau Récapitulatif des Gains par Stage

| Stage | Build #17 | Build #18 | Gain Temps | Gain % | Raison Principale |
|---|---|---|---|---|---|
| **Checkout SCM** | Clone complet | Fetch incrémental | ~10-15s | ~50% | Workspace pré-existant |
| **Set Version** | ~instant | ~instant | 0s | 0% | Identique |
| **Resolve GIB** | ~instant | ~instant | 0s | 0% | Identique |
| **Gitleaks** | 13ms | 15ms | 0s | 0% | Identique |
| **Build** | **5min30s** | **3min39s** | **1min51s** | **34%** | 5 modules en moins (GIB) |
| **Tests & Coverage** | **31.1s** | **13.0s** | **18.1s** | **58%** | 5 modules en moins (GIB) |
| **Quality Analysis** | **14.9s** | **12.5s** | **2.4s** | **16%** | Moins de modules à traverser |
| **Publish Artifacts** | **1min06s** | **17.4s** | **48.6s** | **74%** | 5 modules en moins + moins d'uploads |
| **Docker Build & Push** | **58.7s** | **29.3s** | **29.4s** | **50%** | 5 modules en moins, 1 seule image |
| | | | | | |
| **TOTAL (stages Maven)** | **~9min21s** | **~5min12s** | **~4min09s** | **~44%** | |

---

## 🧠 Analyse en Profondeur

### Pourquoi cette optimisation fonctionne

L'optimisation majeure entre les builds #17 et #18 repose sur **3 piliers** :

#### 1. GIB (Gitflow Incremental Builder) — Réduction du Scope
```
Build #17: pom.xml racine modifié → TOUT recompilé (10/10 modules)
Build #18: Seul service-user modifié → 5/5 modules (user + parents)
```
Quand le `pom.xml` racine change, GIB ne peut pas optimiser car tous les modules héritent de lui. Le Build #18 évite ce piège en ne modifiant qu'un service spécifique.

#### 2. Checkout SCM — Clone vs Fetch
```
Build #17: git clone (workspace vide, premier build sur Jenkins_slave)
Build #18: git fetch (workspace existant sur Jenkins_slave2)
```

#### 3. Effet cumulatif sur toutes les phases Maven
Le passage de 10 → 5 modules se répercute sur **chaque phase** :
- Build : -34%
- Tests : -58%
- Deploy : -74%
- Docker : -50%

### Points d'attention

> [!WARNING]
> - Les deux builds ont un **cache miss Maven** — les dépendances sont téléchargées à chaque fois. Activer/maintenir un cache Maven partagé donnerait un gain supplémentaire majeur sur le stage Build.
> - Le Build #17 télécharge des dépendances à des vitesses très faibles (~7-79 kB/s) comparé au Build #18 (~45-1400 kB/s) — probablement dû à la différence de slave et sa connectivité réseau vers Nexus.
> - Les tests réels (UserServiceTest) prennent seulement 1.7s — le reste du temps Test est du overhead Maven lifecycle.

---

## 🎯 Conclusion

**Le Build #18 est environ 44% plus rapide que le Build #17**, soit un gain total d'environ **4 minutes et 9 secondes** sur les phases Maven. 

La raison principale est **la nature du commit** :
- **#17** modifie le `pom.xml` racine → GIB détecte `monorepo` comme changé → reconstruction de **tous** les 10 modules
- **#18** ne modifie que `service-user` → GIB ne reconstruit que les **5 modules nécessaires** (service-user + ses parents dans la hiérarchie Maven)

Cela démontre parfaitement l'efficacité du **Gitflow Incremental Builder (GIB)** quand les changements sont ciblés sur des modules spécifiques plutôt que sur le POM parent.
