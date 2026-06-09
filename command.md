# Commands and Permissions

noasaba / nanosize

## 一般向けコマンド

| 権限名 | チャットコマンド | 使い方の例 | 具体的な解説 |
|---|---|---|---|
| `betterrtp.use` | `/rtp` | `/rtp` | ランダムな安全地点へテレポートします。 |
| `betterrtp.version` | `/rtp version` | `/rtp version` | 実行中の BetterRTP バージョンを表示します。 |
| `betterrtp.world` | `/rtp world <world>` | `/rtp world world_nether` | 指定ワールドでランダムテレポートします。 |
| `betterrtp.biome` | `/rtp biome <biome>` | `/rtp biome plains` | 指定バイオームを条件にランダムテレポートします。 |
| `betterrtp.location` | `/rtp location <location>` | `/rtp location spawn-area` | 設定済みロケーションを条件にランダムテレポートします。 |
| `betterrtp.group.*` |  |  | 権限グループごとの制限設定を使用できます。 |

## 管理者向けコマンド・権限

| 権限名 | チャットコマンド | 使い方の例 | 具体的な解説 |
|---|---|---|---|
| `betterrtp.reload` | `/rtp reload` | `/rtp reload` | 設定ファイルを再読み込みします。 |
| `betterrtp.player` | `/rtp player <player>` | `/rtp player Steve` | 指定したプレイヤーをランダムテレポートします。 |
| `betterrtp.info` | `/rtp info` | `/rtp info` | RTP 対象ワールドの設定情報を表示します。 |
| `betterrtp.test` | `/rtp test` | `/rtp test` | デバッグ有効時にパーティクル、ポーション効果、サウンドをテストします。 |
| `betterrtp.edit` | `/rtp edit` | `/rtp edit` | カスタムまたはデフォルトワールドの RTP 中心座標や半径を編集します。 |
| `betterrtp.updater` |  |  | 更新通知を受け取れます。 |
| `betterrtp.sign` |  |  | RTP 看板を作成できます。 |
| `betterrtp.world.*` |  |  | すべての有効ワールドで RTP を使用できます。 |
| `betterrtp.bypass.cooldown` |  |  | クールダウン制限を回避できます。 |
| `betterrtp.bypass.delay` |  |  | テレポート待機時間を回避できます。 |
| `betterrtp.bypass.economy` |  |  | 経済コストを回避できます。 |
| `betterrtp.bypass.hunger` |  |  | 空腹度制限を回避できます。 |
| `betterrtp.bypass.*` |  |  | BetterRTP の各種 bypass 権限をまとめて付与します。 |
| `betterrtp.*` |  |  | BetterRTP の包括権限です。通常プレイヤーには付与しません。 |
