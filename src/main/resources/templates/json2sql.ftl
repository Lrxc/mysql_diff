<#if insertParam??>
    <#list insertParam as insertDTO>
INSERT INTO ${tableName} (<#list insertDTO.keys as key>${key}<#if key_has_next>,</#if></#list>) VALUES (<#list insertDTO.values as value>"${value}"<#if value_has_next>,</#if></#list>);
    </#list>
</#if>
