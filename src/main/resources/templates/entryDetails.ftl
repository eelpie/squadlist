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
                                #if($member.dateOfBirth)
                                    $dateFormatter.dayMonthYear($member.dateOfBirth)
                                    </br>
                                    #set($ageGrade = "")
                                    #set($ageGrade = $governingBody.getAgeGrade($governingBody.getEffectiveAge($member.dateOfBirth)))
                                    <span #if($ageGrade) title="$ageGrade" #end>$governingBody.getEffectiveAge($member.dateOfBirth)</span>
                                    #if($ageGrade) <br/> $ageGrade #end
                                #end
                            </td>
                            <td>$!member.weight</td>
                            <td><span #if($member.rowingPoints) title="$governingBody.getRowingStatus($member.rowingPoints)" #end >$!member.rowingPoints</span>
                                <br/>#if($member.rowingPoints) $!governingBody.getRowingStatus($member.rowingPoints) #end
                            </td>
                            <td><span #if($member.scullingPoints) title="$governingBody.getRowingStatus($member.scullingPoints)" #end >$!member.scullingPoints</span>
                                #if($member.scullingPoints) <br/>$!governingBody.getScullingStatus($member.scullingPoints) #end
                            </td>
                            <td>
                                #set($registrationNumberProblems = '')
                                #set($registrationNumberProblems = $governingBody.checkRegistrationNumber($member.registrationNumber))
                                #if($registrationNumberProblems && $registrationNumberProblems != '')
                                    $!member.registrationNumber
                                    <div class="alert alert-danger">
                                        $registrationNumberProblems
                                    </div>
                                #end
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