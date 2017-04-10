<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <jsp:directive.page isELIgnored="false"/> 
<!DOCTYPE html>
<html lang="en">
<head>
<title>Database</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<link rel="stylesheet"
	href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
<link rel="stylesheet"
	href="http://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css">
<link rel="stylesheet"
	href="http://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.11.2/css/bootstrap-select.min.css">
<link rel="stylesheet" type="text/css" href="style.css">
    <link rel="stylesheet" href="codemirror/plugin/codemirror/lib/codemirror.css">


</head>
<body>

	<div class="container">
		<fieldset>
			<legend>Polystore</legend>


			<div class="panel panel-default">

					<form class="form-horizontal" method="POST"
						action="controllerQuery">
							<div class="panel-body">				
						</div>

						<div class="form-group">
							<label class="control-label col-sm-2">Query Sql:</label>
							<div class="col-md-8">
								<textarea id="mytextarea" class="form-control"
									name="querySQL" placeholder="SELECT persona.nome FROM persona, scuola WHERE persona.scuola_id = scuola.id AND scuola.nome = 'Di Maggio'" required></textarea>
							</div>
						</div>
                                               <div class="form-group">
							<label class="control-label col-sm-2">Query Cypher:</label>
							<div class="col-md-8">
								<textarea id="mytextarea" class="form-control"
									name="queryCypher" placeholder="MATCH (indirizzo:indirizzo) WHERE indirizzo.nome = 'brava' RETURN indirizzo.id" required></textarea>
							</div>
						</div>
<div class="panel-body">
								<div class="form-group">
							<label class="control-label col-sm-2">Tabelle:</label>
									<div class="col-md-4">
										<select class="selectpicker" title="Tabelle" name="tabella">
											<option value="{database:'postgres', query:'select * from persona'}" data-content="<div title='persona(id,nome,cognome,scuola_id)' data-placement='right'>Persona</div>" >Persona</option>
											<option  value="Mysql" data-content="<div title='Relationship with project and person' data-placement='right'>Department</div>">Department</option>
											<option  value="Mysql">Hobby</option>
											<option  value="Mysql">Interest</option>
										</select>
									</div>

								</div>
							
						</div>

						<div class="form-group">
							<div class="col-sm-offset-2 col-sm-10">
								<button type="submit" class="btn btn-default">Invia</button>
							</div>
						</div>
					</form>
			</div>
			i risultati potrei farli vedere qui
			<pre>${result}</pre>
		</fieldset>

	</div>

	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
	<script
		src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
		integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
		crossorigin="anonymous"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.11.2/js/bootstrap-select.min.js"></script>
		<script src="script.js"></script>



</body>
</html>
