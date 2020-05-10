import { shallowMount } from '@vue/test-utils'
import Profile from "../../src/components/Profile";

let wrapper;

beforeEach(() => {
    wrapper = shallowMount(Profile, {
        propsData: {},
        mocks: {},
        stubs: {},
        methods: {},
    });
});

afterEach(() => {
    wrapper.destroy();
});

describe('AddActivity.vue', () => {
    test('Computed fitness statement property works given a fitness level', () => {
        wrapper.vm.profile = {"fitness_statement": 3}
        expect(wrapper.vm.fitnessStatement).toContain("Advanced");
    });
    test('Default fitness statement as beginner when fitness level not given', () => {
        expect(wrapper.vm.fitnessStatement).toContain("Beginner")
    });
});