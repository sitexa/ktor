<#-- @ftlvariable name="date" type="java.lang.Long" -->
<#-- @ftlvariable name="code" type="java.lang.String" -->
<#-- @ftlvariable name="sweet" type="com.sitexa.sweet.model.Sweet" -->
<#import "template.ftl" as layout />

<@layout.mainLayout title="New sweet">
<section class="post">
    <header class="post-header">
        <p class="post-meta">
            <a href="/sweet/${sweet.id}">${sweet.date.toDate()?string("yyyy.MM.dd HH:mm:ss")}</a>
            by ${sweet.userId}</p>
    </header>
    <div class="post-description">${sweet.text}</div>
</section>
<#if user??>
<p>
    <a href="javascript:void(0)" onclick="document.getElementById('deleteForm').submit()">Delete sweet</a>
</p>

<form id="deleteForm" method="post" action="/sweet/${sweet.id}/delete" enctype="application/x-www-form-urlencoded">
    <input type="hidden" name="date" value="${date?c}">
    <input type="hidden" name="code" value="${code}">
</form>
</#if>

</@layout.mainLayout>