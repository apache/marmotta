<html>
    <head>
        <title><@ldpath path="rdfs:label[@en]"/></title>
    </head>

    <body>
        <h1><@ldpath path="rdfs:label[@en]"/></h1>

        <p>
            <@ldpath path="rdfs:comment[@en]"/>
        </p>

        <ul>
            <@ldpath path="fn:sort(rdf:type)">
                <#if evalLDPath("rdfs:label[@en] :: xsd:string")??>
                    <li><@ldpath path="rdfs:label[@en] :: xsd:string"/></li>
                </#if>
            </@ldpath>
        </ul>
    </body>

</html>