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

/* eslint-env node*/

const globby = require('globby');
const lex = require('pug-lexer');
const parse = require('pug-parser');
const walk = require('pug-walk');
const compileAttrs = require('pug-attrs');
const {promisify} = require('util');
const fs = require('fs');
const readFileAsync = promisify(fs.readFile);
const fetch = require('node-fetch');
const ProgressBar = require('progress');
const chalk = require('chalk');

const {argv} = require('yargs')
.option('pugs', {
    alias: 'p',
    describe: 'glob pattern to select templates with',
    default: './{app,views}/**/*.pug'
})
.option('domain', {
    alias: 'd',
    describe: 'docs domain to look for in URLs',
    default: 'apacheignite.readme.io'
})
.usage('Usage: $0 [options]')
.example(
    '$0 --pugs="./**/*.pug" --domain="apacheignite.readme.io"',
    `look for all invalid apacheignite.readme.io domain links in all pug files in current dir and it's subdirs`
)
.help();

const pugPathToAST = async(pugPath) => Object.assign(parse(lex(await readFileAsync(pugPath, 'utf8'))), {filePath: pugPath});
const findLinks = (acc, ast) => {
    walk(ast, (node) => {
        if (node.attrs) {
            const href = node.attrs.find((attr) => attr.name === 'href');
            if (href) {
                const compiledAttr = compileAttrs([href], {
                    terse: false,
                    format: 'object',
                    runtime() {}
                });
                try {
                    acc.push([JSON.parse(compiledAttr).href, ast.filePath]);
                } catch (e) {
                    return;
                }
            }
        }
    });
    return acc;
};

const isDocsURL = (url, domain) => url.includes(domain);
const isInvalidURL = (url) => fetch(url).then((res) => res.status === 404);
const first = ([value]) => value;

const checkDocLinks = async(pugsGlob, domain, onProgress = () => {}) => {
    const pugs = await globby(pugsGlob);
    const allAST = await Promise.all(pugs.map(pugPathToAST));
    const allLinks = allAST.reduce(findLinks, []).filter((pair) => isDocsURL(first(pair), domain));
    onProgress(allLinks);
    const tick = (v) => {onProgress(v); return v;};
    const results = await Promise.all(allLinks.map((pair) => {
        return isInvalidURL(first(pair))
        .then((isInvalid) => [...pair, isInvalid])
        .then(tick)
        .catch(tick);
    }));
    const invalidLinks = results.filter(([,, isInvalid]) => isInvalid);
    return {allLinks, invalidLinks};
};

module.exports.checkDocLinks = checkDocLinks;

const main = async() => {
    let bar;
    const updateBar = (value) => {
        if (!bar)
            bar = new ProgressBar('Checking links [:bar] :current/:total', {total: value.length});
        else
            bar.tick();
    };
    console.log(`Looking for invalid ${chalk.cyan(argv.domain)} domain doc links in ${chalk.cyan(argv.pugs)}.`);
    const {allLinks, invalidLinks} = await checkDocLinks(argv.pugs, argv.domain, updateBar);
    const invalidCount = invalidLinks.length;
    const format = ([link, at], i) => `\n${i + 1}. ${chalk.red(link)} ${chalk.dim('in')} ${chalk.yellow(at)}`;
    console.log(`Total links: ${allLinks.length}`);
    console.log(`Invalid links found: ${invalidCount ? chalk.red(invalidCount) : chalk.green(invalidCount)}`);
    if (invalidCount) console.log(invalidLinks.map(format).join(''));
};

main();
