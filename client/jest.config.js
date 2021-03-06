module.exports = {
    verbose: true,
    moduleFileExtensions: [
        "js",
        "json",
        "vue"
    ],
    transformIgnorePatterns: [
        '<rootDir>/node_modules/(?!vee-validate/dist/rules)',
    ],
    transform: {
        ".*\\.(vue)$": "vue-jest",
        "^.+\\.js$": "<rootDir>/node_modules/babel-jest",
        'vee-validate/dist/rules': 'babel-jest',
    },
    collectCoverage: true,
    collectCoverageFrom: [
        "src/components/*/*.{js,vue}",
        "!**/node_modules/**"
    ],
    coverageReporters: [
        "html",
        "text-summary",
        "lcov",
        "json"
    ],
    setupFilesAfterEnv: ['<rootDir>/tests/unit/setup.js'],
    testResultsProcessor: "jest-sonar-reporter",
}