#if($members)
<div class="row">
	<div class="col-xs-3">
		<p>
		#foreach($member in $members)
			$member.displayName<br/>
		#end
		</p>
		
		<span class="badge #if($ok) alert-success #end">$members.size()</span>
		#if ($ok) 
			<span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
		#end		
	</div>
		
	#if ($ok)	
		<div class="col-xs-3">
			#if($rowingStatus)
				<p>$rowingPoints rowing points</p>
				<p>$rowingStatus <span class="glyphicon glyphicon-ok" aria-hidden="true"></span></p>
			#else
				<p>Some crew members have not entered rowing points</p>
			#end
		</div>
		<div class="col-xs-3">
			#if($scullingStatus)
				<p>$scullingPoints sculling points</p>
				<p>$scullingStatus <span class="glyphicon glyphicon-ok" aria-hidden="true"></span></p>
			#else
				<p>Some crew members have not entered sculling points</p>
			#end	
		</div>
		<div class="col-xs-3">
			#if($effectiveAge)
				<p>Effective age: $effectiveAge</p>
				#if($ageGrade)
					<p>$ageGrade <span class="glyphicon glyphicon-ok" aria-hidden="true"></span></p> 
				#end
			#else
				<p>Some crew members have not entered a date of birth</p>
			#end
		</div>
		
	#else
		<div class="col-xs-3">	
			<p>Not a complete boat</p>
		</div>
	#end
			
</div>
#end
<hr/>