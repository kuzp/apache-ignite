/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

lexer grammar IgniteSqlLexer;

@lexer::header {
package org.apache.ignite.internal.parser;
}

SPACE: [ \t\r\n]+ -> channel(HIDDEN);

DROP: D R O P;
INDEX: I N D E X;
IF: I F;
EXISTS: E X I S T S;
TABLE: T A B L E;

SEMI: ';';
DOT: '.';

ID: ( ID_LITERAL | DQUOTE_STRING { setText(getText().substring(1, getText().length() - 1)); });

ID_LITERAL: [A-Za-z_][A-Za-z_0-9]*;
DQUOTE_STRING: '"' ( '\\'. | '""' | ~('"'| '\\') )* '"';

// solving case-sensitivity for keywords
fragment A: [Aa];
fragment B: [Bb];
fragment C: [Cc];
fragment D: [Dd];
fragment E: [Ee];
fragment F: [Ff];
fragment G: [Gg];
fragment H: [Hh];
fragment I: [Ii];
fragment J: [Jj];
fragment K: [Kk];
fragment L: [Ll];
fragment M: [Mm];
fragment N: [Nn];
fragment O: [Oo];
fragment P: [Pp];
fragment Q: [Qq];
fragment R: [Rr];
fragment S: [Ss];
fragment T: [Tt];
fragment U: [Uu];
fragment V: [Vv];
fragment W: [Ww];
fragment X: [Xx];
fragment Y: [Yy];
fragment Z: [Zz];