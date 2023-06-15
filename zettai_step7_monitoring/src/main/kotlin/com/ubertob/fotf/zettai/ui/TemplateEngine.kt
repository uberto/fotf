package com.ubertob.fotf.zettai.ui

import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.OutcomeError
import com.ubertob.fotf.zettai.fp.asOutcome

data class TemplateError(override val msg: String) : OutcomeError

typealias TemplateOutcome = Outcome<TemplateError, Template>

typealias Template = CharSequence

typealias TagMap = Map<String, TemplateTag>

sealed class TemplateTag
data class StringTag(val text: String?) : TemplateTag()
data class ListTag(val tagMaps: List<TagMap>) : TemplateTag()
data class BooleanTag(val bool: Boolean) : TemplateTag()


infix fun String.tag(value: String?): Pair<String, TemplateTag> =
    this to StringTag(value)

infix fun String.tag(value: List<TagMap>): Pair<String, TemplateTag> = this to ListTag(value)

infix fun String.tag(value: Boolean): Pair<String, TemplateTag> = this to BooleanTag(value)

fun Template.renderTemplate(data: TagMap): TemplateOutcome =
    applyAllTags(data).checkForUnappliedTags()

private fun Template.applyAllTags(data: TagMap) = data.entries.fold(this) { text, (k, v) ->
    text.applyTag(k, v)
}

val tagRegex = """\{(.*?)}""".toRegex()

private fun Template.areTagsPresent() = tagRegex.containsMatchIn(this)

private fun Template.checkForUnappliedTags(): TemplateOutcome =
    asOutcome(areTagsPresent().not()) {
        TemplateError("Mappings missing for tags: ${findAllTags(this)}")
    }

private fun findAllTags(text: Template): Template =
    tagRegex.findAll(text).map(MatchResult::value).toSet().joinToString()

private fun Template.applyTag(key: String, tag: TemplateTag): Template =
    when (tag) {
        is StringTag -> applyTemplateTag(key, tag.text)
        is BooleanTag -> applyBooleanTag(key, tag.bool)
        is ListTag -> applyListTag(key, tag.tagMaps)
    }

private fun Template.applyTemplateTag(key: String, tagText: String?): Template =
    replace("""\{${key}}""".toRegex(), tagText.orEmpty())

private fun Template.applyBooleanTag(key: String, keep: Boolean): Template =
    replace(key.toTagRegex()) {
        if (keep) it.value.stripTags(key) else ""
    }


private fun Template.applyListTag(key: String, tagMaps: List<TagMap>): Template =
    replace(key.toTagRegex()) {
        it.value.stripTags(key).generateMulti(tagMaps)
    }


private fun Template.generateMulti(tagMaps: List<TagMap>): Template =
    tagMaps.joinToString(separator = "\n", transform = this::applyAllTags)


private fun Template.toTagRegex() = """\{$this}(.*?)\{/$this}""".toRegex(RegexOption.DOT_MATCHES_ALL)

private fun Template.stripTags(tagName: Template): Template =
    substring(tagName.length + 2, length - tagName.length - 3)


