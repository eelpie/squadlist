<#include 'includes/head.ftl'>
#navTabs('My outings')

<span id="spinner" style="display: none"><img src="$urlBuilder.staticUrl('wait30.gif')"></span>
												
<div class="container-fluid">

	<div class="row">
		<div class="col-xs-10">
			<h4>${title!}</h4>
		</div>
		<div class="col-xs-2">
			<span class="pull-right">
				<a href="${icalUrl}">iCal</a>
			</span>
		</div>
	</div>
	<hr/>

	<#if member.squads?size == 0>
		<h5><@spring.message 'not.assigned.to.any.squads' /></h5>
		<p><@spring.message 'no.outing.because.not.assigned.to.any.squads' /></p>
			
	<#else>
	
		<#if outings?size == 0>
		   <div class="row">
		   		<div class="col-md-12">
					<h5><@spring.message 'no.current.outings' /></h5>
					<p><@spring.message 'no.current.outings.for.your.squads' /></p>
				</div>
			</div>

		<#else>
			<div class="row">
				<div class="col-md-12">
					<form>

						<p>$squadNamesHelper.list($member.squads) outings.</p>

						<table style="font-size: 14px;" class="table table-striped">
							<#list outings as outingAvailability>
							    <tr>
						   			<td>
						   				#set($outing = $outingAvailability.outing)
										#parse('includes/outing.vm')
								    </td>
						   			<td>
						   				#if($outing.closed)
						   					#parse('includes/closedOuting.vm')

						   				#else
											<select class="availabilityDropdown" id="${outingAvailability.outing.id}">
												<option value="">TBA</option>
												#foreach($option in $availabilityOptions)
													#set($selected = '')
													#if ($outingAvailability && $outingAvailability.availabilityOption.id == $option.id)
														#set($selected = 'SELECTED')
													#end
													<option value="$option.id" $selected>$option.label</option>
												#end
											</select>
										#end
									</td>
									<td>
										#if ($outingAvailability.availabilityOption)
											<span id="currentAvailability${outingAvailability.outing.id}" class="pull-right">
												#set($availability = $outingAvailability.availabilityOption)
												#parse('includes/availability.vm')
											</span>

										#else
							 				<span class="pull-right" id="currentAvailability${outingAvailability.outing.id}">TBA</span>
										#end
								    </td>
								</tr>
							</#list>
						</table>
					</form>
				</div>
			</div>
		</#if>
	</#if>
</div>
<hr/>

<script>
	$('.availabilityDropdown').change(function() {
    	outingId = $(this).attr('id');
    	newAvailability = $(this).val();

		$("#currentAvailability" + outingId).html($('#spinner').html())

		updateUrl = "${urlBuilder.applicationUrl('/availability/ajax')}";
		postData = { outing: outingId, availability: newAvailability };
    	redrawPendingOutingBadge = function(data) {$("#pendingOutings").html(data)};
		pendingOutingsCountUrl = '$urlBuilder.applicationUrl('/myoutings/ajax')';

	    redrawCurrentOutingAvailabilityAndPendingOutingBadge = function(data) {
			$("#currentAvailability" + outingId).html(data);
			$.post(pendingOutingsCountUrl, {}, redrawPendingOutingBadge);
		};

    	$.ajax(
    			{
    				url: updateUrl,
    				type: 'POST',
    				data: postData,
    				dataType: 'text',
    				success: function(data, status, xhr) {
    					redrawCurrentOutingAvailabilityAndPendingOutingBadge(data);
    				},
    				error: function(xhr, status, error) {
    					$("#currentAvailability" + outingId).html("Error: could not be updated");
    				}
    			}
    	);

  	});
</script>

<#include 'includes/footer.ftl'>
