# Idle Load Fix Task Breakdown

noasaba / nanosize

## 前提・禁止事項

- Maven 構成のまま対応する。
- Java 25 上で動作することを前提に実装・検証する。
- 既存の権限名、コマンド名、alias は変更しない。
- `plugin.yml` の `commands.betterrtp` は維持する。
- 既存 alias の `brtp`, `rtp`, `randomtp`, `wild`, `wildtp` は維持する。
- 既存権限の `betterrtp.*`, `betterrtp.use`, `betterrtp.world.*`, `betterrtp.bypass.*`, `betterrtp.player`, `betterrtp.reload`, `betterrtp.updater`, `betterrtp.biome`, `betterrtp.version`, `betterrtp.location`, `betterrtp.group.*`, `betterrtp.sign`, `betterrtp.info`, `betterrtp.test`, `betterrtp.edit` は名前を変えない。
- Addons 側の `betterrtp.addon.portals` も名前を変えない。
- 既存 config key は削除・改名しない。変更が必要な場合は互換読み取りか migration を入れる。
- Paper 専用 API を必須にしない。Spigot でロードできる状態を維持する。

## ゴール

BetterRTP を未使用の状態で起動しているだけのときに、不要なチャンクロード、SQLite 処理、再試行タスクが継続して走らないようにする。

## 対応タスク

### 1. Queue 事前生成の起動条件を見直す

- 対象:
  - `src/main/java/me/SuperRonanCraft/BetterRTP/BetterRTP.java`
  - `src/main/java/me/SuperRonanCraft/BetterRTP/references/rtpinfo/QueueHandler.java`
  - `src/main/java/me/SuperRonanCraft/BetterRTP/references/rtpinfo/QueueGenerator.java`
  - `src/main/resources/config.yml`
- 現状:
  - `Settings.Queue.Enabled: true` がデフォルト。
  - 起動・reload 時の `queue.load()` から、プレイヤーが `/rtp` を使っていなくても安全地点生成が始まる。
- 作業:
  - Queue 生成を「起動直後に常時補充」から「必要時に補充」へ寄せる。
  - 既存 key `Settings.Queue.Enabled` は維持する。
  - デフォルト値を変更する場合は patch version bump と README/command.md 影響確認を行う。
  - `/rtp queue` の既存コマンド名・権限名は変更しない。
- 受け入れ条件:
  - 起動後、誰も RTP を使わない状態で QueueGenerator がチャンクロードを開始しない、または明確に設定で抑止できる。
  - `Settings.Queue.Enabled: true` の既存利用者に対し、挙動変更を changelog/README に明記できる状態にする。

### 2. Circle Queue 範囲判定バグを修正する

- 対象:
  - `src/main/java/me/SuperRonanCraft/BetterRTP/references/rtpinfo/QueueHandler.java`
- 現状:
  - `isInCircle` が距離の二乗ではなく `(center_x - x) * 2 + (center_z - z) * 2` を使っている。
  - 生成済み Queue が範囲内と判定されず、追加生成を誘発する可能性がある。
- 作業:
  - `dx * dx + dz * dz` と `radius * radius` / `radius_min * radius_min` で比較する。
  - int overflow を避けるため `long` で計算する。
- 受け入れ条件:
  - `Shape: circle` で min/max radius 内の地点だけ true になる。
  - `Shape: square` の既存判定・権限・コマンドには影響しない。

### 3. Queue 生成の再試行制御を安全にする

- 対象:
  - `src/main/java/me/SuperRonanCraft/BetterRTP/references/rtpinfo/QueueGenerator.java`
  - `src/main/java/me/SuperRonanCraft/BetterRTP/versions/AsyncHandler.java`
- 現状:
  - 失敗時に `queueGenerator(reQueueData)` が連鎖する。
  - `queueMaxAttempts` 到達時に `generating` が false に戻らない分岐がある。
  - DB 未ロード時は 10 tick ごとに再スケジュールする。
- 作業:
  - 最大試行到達時・cancel 時・例外時に必ず `generating = false` になるようにする。
  - DB 未ロード再試行に上限または backoff を入れる。
  - reload/unload 後に古い task が復活しないよう generation id などで無効化する。
- 受け入れ条件:
  - DB がロードできない状態でも短周期タスクが無限に増えない。
  - `/rtp` 使用後の補充は必要な分だけ走る。
  - reload 連打で QueueGenerator の task が多重化しない。

### 4. Queue DB の検索負荷を下げる

- 対象:
  - `src/main/java/me/SuperRonanCraft/BetterRTP/references/database/DatabaseQueue.java`
  - `src/main/java/me/SuperRonanCraft/BetterRTP/references/database/SQLite.java`
- 現状:
  - `ORDER BY RANDOM()` を範囲検索に使っている。
  - Queue テーブルに `world`, `x`, `z` 用 index がない。
- 作業:
  - `Queue(world, x, z)` の index を idempotent に作成する migration を追加する。
  - `ORDER BY RANDOM()` を避ける方式を検討する。
  - SQL は文字列連結ではなく prepared statement に寄せる。
- 受け入れ条件:
  - 既存 DB でも複数回 migration して壊れない。
  - Queue 件数が多い状態でも範囲検索が過度に重くならない。
  - DB schema 変更として version bump と migration 影響を明記する。

### 5. Java 25 実行環境で検証する

- 対象:
  - `pom.xml`
  - `BetterRTPAddons/pom.xml`
  - CI またはローカル検証手順
- 作業:
  - `java -version` と `mvn -version` で JDK 25 を使っていることを確認する。
  - Java 25 上で `mvn -DskipTests package` を通す。
  - 必要なら Maven Compiler Plugin を Java 25 環境で問題ない版へ更新する。
  - class file target を上げる場合は、Spigot サーバー実行環境との互換性を確認してから行う。
- 受け入れ条件:
  - Java 25 上で BetterRTP と BetterRTPAddons が package できる。
  - Java 25 上の Spigot で plugin load が成功する。
  - 起動時ログに重大例外が出ない。

### 6. 回帰テスト・計測を追加する

- 対象:
  - Queue 判定ロジック
  - Queue 生成制御
  - DB migration
- 作業:
  - `isInCircle` の単体テストを追加する。
  - QueueGenerator の state 遷移をテスト可能な形に切り出す。
  - Java 25 で package できる検証手順を README または別 docs に記録する。
  - 実サーバー検証では起動後 5 分間、RTP 未使用時の task 数、chunk load、SQLite 書き込みを確認する。
- 受け入れ条件:
  - Circle/Square の範囲判定が明確にテストされる。
  - 未使用時に Queue 補充が走らない、または設定通りの低頻度に制御されることを確認できる。

## 実装順

1. `isInCircle` の距離計算を修正し、テストを追加する。
2. QueueGenerator の `generating` と task lifecycle を修正する。
3. Queue 起動条件を「必要時補充」に変更する。
4. Queue DB index migration を追加する。
5. Java 25 で Maven package と Spigot 起動確認を行う。
6. README/changelog/command.md の影響有無を確認する。

## 実装進捗

- 着手済み: 起動・reload 時の Queue 全ワールド事前生成を停止し、必要時補充へ寄せる。
- 着手済み: `isInCircle` の距離計算を `long` の二乗距離判定へ修正する。
- 着手済み: QueueGenerator の task generation id、DB load wait backoff、例外時の `generating` 復帰を追加する。
- 着手済み: Queue DB に `idx_queue_world_x_z` を idempotent に作成する。
- 着手済み: Queue 範囲検索から `ORDER BY RANDOM()` を外し、index 範囲 count と random offset に変更する。
- 完了: Maven が未導入だったため Homebrew で Maven 3.9.16 を導入し、Java 25.0.2 上で BetterRTP / BetterRTPAddons の Maven package を確認した。
- 完了: Java 25 対応として Lombok を 1.18.46、Maven Compiler Plugin を 3.14.0 へ更新した。
- 完了: Maven 解決失敗を避けるため、フル `spigot` 依存を外して `spigot-api` に寄せ、問題のある provided plugin 依存の推移依存を除外した。
- 未完了: Spigot 実サーバーでの Java 25 起動確認。
- 未完了: 自動テスト追加。

## バージョニング方針

- Circle 判定修正のみなら patch bump。
- Queue 起動条件のデフォルト挙動を変える場合も patch bump。ただし既存運用への影響が大きい場合は minor bump を検討する。
- DB schema/index migration を追加する場合は patch bumpでよいが、破壊的 schema 変更はしない。
- コマンド名、alias、権限名を変える変更は禁止。もし避けられない場合は major bump 対象だが、この対応範囲では行わない。

## 確認コマンド

```sh
java -version
mvn -version
mvn -DskipTests package
mvn -DskipTests install
(cd BetterRTPAddons && mvn -U -DskipTests package)
```

実サーバー検証:

```text
Java: 25
Server: Spigot target 26.1.2
Plugin: BetterRTP
Scenario: 起動後、RTP コマンド未使用で 5 分放置
Expected: Queue 補充による継続的なチャンクロード・SQLite 書き込みが発生しない、または設定された範囲に収まる
```
