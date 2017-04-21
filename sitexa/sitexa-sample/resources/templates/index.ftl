<#-- @ftlvariable name="top" type="java.util.List<com.sitexa.sweet.model.Sweet>" -->
<#-- @ftlvariable name="latest" type="java.util.List<com.sitexa.sweet.model.Sweet>" -->

<#import "template.ftl" as layout />

<@layout.mainLayout title="Welcome">
<div class="posts">
    <h3 class="content-subhead">Top 10</h3>
    <@layout.sweets_list sweets=top></@layout.sweets_list>

    <h3 class="content-subhead">Recent 10</h3>
    <@layout.sweets_list sweets=latest></@layout.sweets_list>
</div>
</@layout.mainLayout>
