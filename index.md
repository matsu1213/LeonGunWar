## やりたいことメモ

やりたいことをここにメモしていきます。

### ダメージ補正の導入

ダメージ補正をゲームの中に取り入れます。これによってバランス調整をします。逆に崩壊するかもしれないので、テストプレイをたくさんやる予定です。

### ダメージ補正の概要

#### 連続ダメージ補正

連続ダメージ補正は、連続してプレイヤーがダメージを受けたときに、ダメージを減算します。

基本的には24F、つまり8tick、0.4秒いないにダメージが入った場合に補正がかかるようにします。

どのぐらい補正をかけるかは、武器の種類によって変わります。基本的には 1/2 ～ 2/3 ぐらいを本来のダメージに乗算します。

#### ヒット後無敵

ヒット後無敵は、主にプレイヤーが大きなダメージを受けたときに、1～2秒程度そのプレイヤーにダメージが入らなくなります。

#### ヒット後技無敵

ヒット後技無敵は、

```markdown
Syntax highlighted code block

# Header 1
## Header 2
### Header 3

- Bulleted
- List

1. Numbered
2. List

**Bold** and _Italic_ and `Code` text

[Link](url) and ![Image](src)
```

For more details see [GitHub Flavored Markdown](https://guides.github.com/features/mastering-markdown/).

### Jekyll Themes

Your Pages site will use the layout and styles from the Jekyll theme you have selected in your [repository settings](https://github.com/matsu1213/LeonGunWar/settings/pages). The name of this theme is saved in the Jekyll `_config.yml` configuration file.

### Support or Contact

Having trouble with Pages? Check out our [documentation](https://docs.github.com/categories/github-pages-basics/) or [contact support](https://support.github.com/contact) and we’ll help you sort it out.
