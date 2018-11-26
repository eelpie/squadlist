<#include 'includes/head.ftl'>
<@navTabs '' />

<div class="container-fluid">

    <div class="row">
        <div class="col-xs-8">
            <h4><@spring.message 'change.password' /></h4>
        </div>
    </div>
    <hr/>

    <div class="row">
        <div class="col-xs-12">
            <@spring.bind "changePassword" />

            <#list spring.status.errorMessages as error>
                <div class="alert alert-warning">${error}</div>
            </#list>

            <form method="POST" action="${urlBuilder.changePassword()}" class="form-horizontal" accept-charset="UTF-8">
                <@spring.bind "changePassword.currentPassword" />
                <div class="form-group">
                    <label for="${spring.status.expression}" class="col-sm-2 control-label"><@spring.message 'current.password' /></label>
                    <div class="col-sm-10">
                        <input type="password" class="form-control" name="${spring.status.expression}" value="${spring.status.value!}" placeholder="<@spring.message 'current.password'/>">
                        <#list spring.status.errorMessages as error>
                            <div class="alert alert-warning">${error}</div>
                        </#list>
                    </div>
                </div>

                <@spring.bind "changePassword.newPassword" />
                <div class="form-group">
                    <label for="${spring.status.expression}" class="col-sm-2 control-label"><@spring.message 'new.password'/></label>
                    <div class="col-sm-10">
                        <input type="password" class="form-control" name="${spring.status.expression}" value="${spring.status.value!}" placeholder="<@spring.message 'new.password'/>">
                        <#list spring.status.errorMessages as error>
                            <div class="alert alert-warning">${error}</div>
                        </#list>
                    </div>
                </div>

                <div class="control-group">
                    <div  class="col-sm-2">
                    </div>
                    <div class="controls">
                        <button type="submit" class="btn btn-primary""><@spring.message 'change.password'/></button>
                        <@cancel urlBuilder.memberUrl(member) />
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
<hr/>

<#include 'includes/footer.ftl'>