/*
 * Copyright 2018 rousakis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.vre4eic.evre.metadata.utils;

/**
 *
 * @author rousakis
 */
public class SPARQLUpdates {

    public static final String matPersonWorkflow = "WITH @#$%FROM%$#@ \n"
            + "INSERT { \n"
            + "  ?pers ?pers_ser ?ser. \n"
            + "  ?ser ?ser_pers ?pers. \n"
            + "} WHERE { \n"
            + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
            + "  ?ser a <http://eurocris.org/ontology/cerif#Workflow>.\n"
            + "\n"
            + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
            + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
            + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
            + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
            + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
            + "\n"
            + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Workflow/\",encode_for_uri(?role) )) as ?pers_ser ). \n"
            + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Workflow-Person/\",encode_for_uri(?role_opposite) )) as ?ser_pers ). \n"
            + "}";
}
