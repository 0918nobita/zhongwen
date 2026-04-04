package engineer.kodai.yanniang.model

data class Word(
    val id: Int,
    /** 漢字 */
    val hanzi: String,
    val pinyin: String,
    val meaning: String,
)

val sampleWords = listOf(
    Word(1, "你好", "nǐ hǎo", "こんにちは"),
    Word(2, "谢谢", "xiè xie", "ありがとう"),
    Word(3, "再见", "zài jiàn", "さようなら"),
    Word(4, "对不起", "duì bu qǐ", "すみません"),
    Word(5, "我爱你", "wǒ ài nǐ", "愛しています"),
    Word(6, "好的", "hǎo de", "わかりました"),
    Word(7, "不是", "bù shì", "違います"),
    Word(8, "什么", "shén me", "何ですか"),
)
