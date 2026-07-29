'use strict';

/*
 * Frontend build for the AngularJS 1.x client.
 * Replaces the legacy Bower + Gulp 3 toolchain: pulls vendor libraries from npm
 * and uses esbuild to concatenate + minify vendor and app bundles, then copies
 * templates, styles, fonts and images into target/dist (served from /public).
 */

const esbuild = require('esbuild');
const fg = require('fast-glob');
const fs = require('fs');
const path = require('path');

const root = __dirname;
const nodeModules = path.join(root, 'node_modules');
const dist = path.join(root, 'target', 'dist');

const ensureDir = (dir) => fs.mkdirSync(dir, { recursive: true });

const copyInto = (srcFile, destDir) => {
  ensureDir(destDir);
  fs.copyFileSync(srcFile, path.join(destDir, path.basename(srcFile)));
};

const minify = (code, loader) =>
  esbuild.transform(code, { minify: true, loader, legalComments: 'none' }).then((r) => r.code);

const concat = (files) => files.map((f) => fs.readFileSync(f, 'utf8')).join('\n;\n');

/*
 * AngularJS distinguishes `angular.module('x', [deps])` (defines) from
 * `angular.module('x')` (retrieves), so a module must be defined before it is
 * retrieved. Each feature folder keeps its definition in <folder>/<folder>.js,
 * which therefore has to be concatenated ahead of the rest of that folder.
 */
const orderAppFiles = (files) => {
  const definesModule = (file) => path.basename(file) === `${path.basename(path.dirname(file))}.js`;
  return files.sort((a, b) => {
    const dirA = path.dirname(a);
    const dirB = path.dirname(b);
    if (dirA !== dirB) return dirA < dirB ? -1 : 1;
    if (definesModule(a) !== definesModule(b)) return definesModule(a) ? -1 : 1;
    return a < b ? -1 : 1;
  });
};

// Fails the build if a module is retrieved before it is defined, which would
// otherwise only surface at runtime as [$injector:nomod] on a blank page.
const assertModuleOrder = (files) => {
  const defined = new Set();
  for (const file of files) {
    const source = fs.readFileSync(file, 'utf8');
    for (const [, name] of source.matchAll(/angular\.module\(\s*['"]([\w.-]+)['"]\s*\)/g)) {
      if (!defined.has(name)) {
        throw new Error(
          `${path.relative(root, file)} uses angular.module('${name}') before it is defined; ` +
            'fix the bundle order in orderAppFiles().'
        );
      }
    }
    for (const [, name] of source.matchAll(/angular\.module\(\s*['"]([\w.-]+)['"]\s*,/g)) {
      defined.add(name);
    }
  }
};

async function build() {
  fs.rmSync(dist, { recursive: true, force: true });
  ensureDir(path.join(dist, 'scripts'));

  // Vendor bundle (order matters: jQuery before Bootstrap; Angular before ui-router).
  const vendorFiles = [
    'jquery/dist/jquery.min.js',
    'angular/angular.min.js',
    '@uirouter/angularjs/release/angular-ui-router.min.js',
    'bootstrap/dist/js/bootstrap.min.js',
  ].map((f) => path.join(nodeModules, f));
  fs.writeFileSync(
    path.join(dist, 'scripts', 'vendor.min.js'),
    await minify(concat(vendorFiles), 'js')
  );

  // App bundle (app.js first so the root module is defined before feature modules).
  const appEntry = path.join(root, 'src', 'scripts', 'app.js');
  const featureFiles = (await fg('src/scripts/**/*.js', { cwd: root, absolute: true })).filter(
    (f) => f !== appEntry
  );
  const appFiles = [appEntry, ...orderAppFiles(featureFiles)];
  assertModuleOrder(appFiles);
  fs.writeFileSync(path.join(dist, 'scripts', 'app.min.js'), await minify(concat(appFiles), 'js'));

  // Component templates (referenced via templateUrl relative to the app root).
  for (const rel of await fg('src/scripts/**/*.html', { cwd: root })) {
    const dest = path.join(dist, rel.replace(/^src\//, ''));
    ensureDir(path.dirname(dest));
    fs.copyFileSync(path.join(root, rel), dest);
  }

  // Application stylesheet.
  ensureDir(path.join(dist, 'css'));
  fs.writeFileSync(
    path.join(dist, 'css', 'petclinic.css'),
    await minify(fs.readFileSync(path.join(root, 'src', 'css', 'petclinic.css'), 'utf8'), 'css')
  );

  // Bootstrap CSS + glyphicon fonts served under /bootstrap (matches index.html paths).
  copyInto(
    path.join(nodeModules, 'bootstrap/dist/css/bootstrap.min.css'),
    path.join(dist, 'bootstrap', 'css')
  );
  for (const rel of await fg('bootstrap/dist/fonts/*', { cwd: nodeModules })) {
    copyInto(path.join(nodeModules, rel), path.join(dist, 'bootstrap', 'fonts'));
  }

  // Application fonts and images.
  for (const rel of await fg('src/fonts/*', { cwd: root })) {
    copyInto(path.join(root, rel), path.join(dist, 'fonts'));
  }
  for (const rel of await fg('src/images/*', { cwd: root })) {
    copyInto(path.join(root, rel), path.join(dist, 'images'));
  }

  console.log('Frontend build complete -> target/dist');
}

build().catch((err) => {
  console.error(err);
  process.exit(1);
});
