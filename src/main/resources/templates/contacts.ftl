<#include 'includes/head.ftl'>
<@navTabs 'Contacts' />

<div class="container-fluid">

    <#if squads?size != 0>
        <div class="row">
            <div class="col-xs-8">
                <h4><@spring.message 'contacts' /></h4>
            </div>
            <div class="col-xs-4">
                <div class="pull-right">
                    <@squadSelect '/contacts/' 'Show contacts' />
                </div>
            </div>
        </div>
        <hr/>

        <#if members?size== 0>
            <p><@spring.message 'no.members.assigned.to.squad' /></p>
        <#else>

            <#if emails?? >
                <p align="right"><a href="${urlBuilder.mailto(emails)}"><@spring.message 'email.members' /></a></p>
            </#if>

            <table style="font-size: 14px;" class="table table-striped">
                <thead>
                    <tr>
                        <th></th>
                        <th><@spring.message 'email' /></th>
                        <th><@spring.message 'phone' /></th>
                    </tr>
                </thead>

                <#list members as member>
                    <tr>
                        <td>
                            <#include 'includes/member.ftl'>
                            <#if member.role?? && member.role != 'Rower' >
                                (${member.role})
                            </#if>
                        </td>
                        <td>
                            <#if member.emailAddress?? >
                                <a href="mailto:${member.emailAddress}">${member.emailAddress}</a>
                            </#if>
                        </td>
                        <td>
                            <#if member.contactNumber?? >
                                <a href="tel:${member.contactNumber}">${member.contactNumber}</a>
                            </#if>
                        </td>
                    </tr>
                </#list>
            </table>
        </#if>
					
    <#else>
        <h4><@spring.message 'contacts' /></h4>
        <hr/>
        <p>There are currently no squads setup.</p>
    </#if>

</div>
<hr/>

<#include 'includes/footer.ftl'>

