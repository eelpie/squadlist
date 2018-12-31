<#include 'includes/head.ftl'>
<@navTabs 'Entry details' />

<div class="container-fluid">

	<#if squads?size != 0>
        <div class="row">
            <div class="col-xs-8">
                <h4><@spring.message 'entry.details' /></h4>
            </div>
            <div class="col-xs-4">
                <div class="pull-right">
                    <@squadSelect '/entry-details/' 'Show entry details' />
                </div>
            </div>
        </div>
        <hr/>

		<#if members?size == 0>
			<p><@spring.message 'no.members.assigned.to.squad' /></p>

		<#else>
            <div id="selectedMembers"></div>
            <table style="font-size: 14px;" class="table table-striped">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                        <th><@spring.message 'effective.age' /></th>
                        <th><@spring.message 'weight' /></th>
                        <th><@spring.message 'rowing' /></th>
                        <th><@spring.message 'sculling' /></th>
                        <th><@spring.message 'registration' /></th>
                    </tr>
                </thead>
                    <form>
                    <#list members as member>
                        <tr>
                            <td><input type="checkbox" class="selected" value="$member.id" /></td>
                            <td><#include 'includes/member.ftl'></td>
                            <td>
                                <#if member.dateOfBirth??>
                                    ${dateFormatter.dayMonthYear(member.dateOfBirth)}
                                    </br>
                                    <#assign effectiveAge = governingBody.getEffectiveAge(member.dateOfBirth) >
                                    <#assign ageGrade = governingBody.getAgeGrade(effectiveAge) >
                                    <span <#if $ageGrade??> title="${ageGrade}" </#if>>${effectiveAge}</span>
                                    <#if ageGrade?? ><br/> ${ageGrade}</#if>
                                </#if>
                            </td>
                            <td>${member.weight!}</td>
                            <td>
                                <#if member.rowingPoints??>
                                    <#assign rowingStatus = governingBody.getRowingStatus(member.rowingPoints) >
                                    <span title="${rowingStatus!}">${member.rowingPoints!}</span>
                                    <br/>${rowingStatus!}
                                </#if>
                            </td>
                            <td>
                                <#if member.scullingPoints??>
                                    <#assign scullingStatus = governingBody.getScullingStatus(member.scullingPoints) >
                                    <span title="${scullingStatus!}">${member.scullingPoints!}</span>
                                    <br/>${scullingStatus!}
                                </#if>
                            </td>
                            <td>
                                ${member.registrationNumber!}
                                <#assign registrationNumberProblems = governingBody.checkRegistrationNumber(member.registrationNumber)>
                                <#if registrationNumberProblems??>
                                    <div class="alert alert-danger">
                                        ${registrationNumberProblems}
                                    </div>
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </form>
            </table>
        </#if>

    <#else>
        <h4><@spring.message 'entry.details' /></h4>
        <hr/>
        <p>There are currently no squads setup.</p>
    </#if>

    <div class="row">
        <div class="col-xs-6">
            <p><@spring.message 'governing.body' />: <a href="${urlBuilder.governingBody(governingBody)}">${governingBody.name}</a><p/>
        </div>
        <div class="col-xs-6">
            <p align="right"><a href="${urlBuilder.entryDetailsCsv(squad)}"><@spring.message 'export.all.as.csv' /></a><p/>
        </div>
    </div>
</div>
<hr/>

<script>
    $('.selected').change(function() {
        var selectedMemberIds = $("input:checkbox:checked").map(function(){
            return $(this).val();
        });

        $.ajax({
            url: "$urlBuilder.applicationUrl('/entry-details/ajax')",
            type: 'POST',
            contentType: 'application/json',
            processData: false,
            data: JSON.stringify(selectedMemberIds.get()),
            success:function(result) {
                    $("#selectedMembers").html(result);
                }
            }
        );

    });
</script>

<#include 'includes/footer.ftl'>