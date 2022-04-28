// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'MMDBot Docs',
  tagline: 'A feature-rich Discord bot',
  url: 'https://minecraftmoddevelopment.github.io/',
  baseUrl: '/MMDBot/',
  projectName: 'MMDBot',
  organizationName: 'minecraftmoddevelopment',
  trailingSlash: true,
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'MinecraftModDevelopment', // Usually your GitHub org/user name.
  projectName: 'MMDBot', // Usually your repo name.

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl: 'https://github.com/MinecraftModDevelopment/MMDBot/docs/docs',
        },
        blog: {
          showReadingTime: true,
          // Please change this to your repo.
          editUrl:
            'https://github.com/MinecraftModDevelopment/MMDBot/docs/blog',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'MMDBot',
        logo: {
          alt: 'MMDBot',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'doc',
            docId: 'setup',
            position: 'left',
            label: 'Get Started',
          },
          {to: '/blog', label: 'Blog', position: 'left'},
          {
            href: 'https://github.com/MinecraftModDevelopment/MMDBot',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Get Started',
                to: '/docs/setup',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'Discord',
                href: 'https://discord.mcmoddev.com',
              }
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'Blog',
                to: '/blog',
              },
              {
                label: 'GitHub',
                href: 'https://github.com/MinecraftModDevelopment/MMDBot',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} MinecraftModDevelopment, Inc. Built with Docusaurus.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

module.exports = config;
