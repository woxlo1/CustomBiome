# CustomBiome

カスタムバイオームを YAML で定義・管理できる PaperMC 1.20.4 プラグインです。  
WorldEdit / WorldGuard 連携と MySQL 永続化をサポートしています。

---

## 動作環境

| 依存 | バージョン |
|---|---|
| PaperMC | 1.20.4 |
| WorldEdit | 7.3.x |
| WorldGuard | 7.0.x |
| Java | 17 以上 |
| MySQL | 8.0 以上 |

**WoxloiDevAPI は不要です。** 本プラグインは標準 Bukkit API と HikariCP のみで動作します。

---

## インストール

1. `./gradlew shadowJar` でビルド
2. `build/libs/CustomBiome-1.0.0.jar` を `plugins/` に配置
3. WorldEdit・WorldGuard も `plugins/` に配置
4. サーバーを起動して `plugins/CustomBiome/config.yml` のデータベース設定を編集
5. サーバーを再起動

---

## config.yml

```yaml
database:
  host: "localhost"
  port: 3306
  name: "custombiome"
  username: "root"
  password: "password"
  useSSL: false
  pool-size: 10

settings:
  biomes-dir: "biomes"        # バイオーム定義YAMLのフォルダ
  prefix: "§e[§b§lCustomBiome§e] §r"

world:
  prefix: "cb_"               # 自動生成ワールド名のプレフィックス

worldguard:
  auto-create-region: true    # WE選択範囲からWGリージョンを自動作成
  region-prefix: "cb_"
```

---

## バイオーム定義 YAML

`plugins/CustomBiome/biomes/` 以下に `.yml` ファイルを置くと自動ロードされます。

```yaml
name: "my_biome"
display-name: "&aマイバイオーム"
description: "説明テキスト"
icon: GRASS_BLOCK

terrain:
  type: HILLS          # FLAT / HILLS / MOUNTAINS / OCEAN / PLATEAU
  min-height: 60
  max-height: 120

blocks:
  surface: GRASS_BLOCK
  subsurface: DIRT
  deep: STONE
  bedrock-layer: DEEPSLATE
  fluid: WATER
  sea-level: 62

features:
  trees:
    enabled: true
    types:
      - type: OAK
        weight: 100
    max-per-chunk: 4
    chance: 0.5
  ores:
    enabled: true
    veins:
      - block: COAL_ORE
        min-height: 0
        max-height: 128
        vein-size: 17
        chance: 0.8

spawns:
  day:
    - entity: COW
      min: 1
      max: 4
      weight: 10
  night:
    - entity: ZOMBIE
      min: 2
      max: 4
      weight: 8
  water: []

weather:
  temperature: 0.5
  humidity: 0.5
  rain-chance: 0.3

ambience:
  particles:
    enabled: false
  sounds: []
```

---

## コマンド

| コマンド | 説明 | 権限 |
|---|---|---|
| `/cbiome list` | バイオーム一覧GUI（コンソールはテキスト） | `custombiome.use` |
| `/cbiome info <key>` | バイオーム詳細GUI | `custombiome.use` |
| `/cbiome create <name>` | バイオームYAMLテンプレート生成 | `custombiome.create` |
| `/cbiome generate <key> [name] [seed]` | カスタムワールド生成 | `custombiome.generate` |
| `/cbiome tp <world>` | カスタムワールドへTP | `custombiome.tp` |
| `/cbiome region set <key>` | WE選択範囲にバイオーム割り当て | `custombiome.region` |
| `/cbiome region assign <id> <key>` | 既存WGリージョンにバイオーム割り当て | `custombiome.region` |
| `/cbiome region clear <id>` | リージョン割り当て解除 | `custombiome.region` |
| `/cbiome region list` | 割り当て済みリージョン一覧 | `custombiome.region` |
| `/cbiome reload` | 設定・バイオームリロード | `custombiome.reload` |

---

## 権限

| 権限 | デフォルト | 説明 |
|---|---|---|
| `custombiome.use` | true | 基本使用 |
| `custombiome.create` | op | テンプレート生成 |
| `custombiome.generate` | op | ワールド生成 |
| `custombiome.tp` | op | テレポート |
| `custombiome.reload` | op | リロード |
| `custombiome.region` | op | リージョン操作 |

---

## WoxloiDevAPI からの変更点

旧バージョンから以下を自前実装に置き換えました。

| 旧（WoxloiDevAPI） | 新（標準実装） |
|---|---|
| `CommandNode` / `CommandRegistry` | `CommandExecutor` / `TabCompleter` |
| `MySQLProvider` / `DatabaseRegistry` / `DatabaseTable` | HikariCP + JDBC 直接実装 |
| `HookManager` | `server.pluginManager.getPlugin()` |
| `WorldEditAPI` / `WorldGuardAPI` | WorldEdit / WorldGuard 直接 API |
| `TaskScheduler` | `server.scheduler.runTask` / `runTaskAsynchronously` |
| `MenuBuilder` / `MenuManager` / `MenuItem` | `InventoryHolder` ベースの `GuiMenu` |

---

## ライセンス

MIT
