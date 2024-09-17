require "spec_helper"

describe "MediaEntry with ImageMediaFile Factory" do
  before :each do
    FactoryBot.create :media_entry
  end
  it "Creates a MediaEntry" do
    expect(MediaEntry.count).to be == 1
  end
end
